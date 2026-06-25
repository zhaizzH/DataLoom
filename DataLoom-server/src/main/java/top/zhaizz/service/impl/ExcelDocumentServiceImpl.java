package top.zhaizz.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.zhaizz.mapper.ExcelDocumentMapper;
import top.zhaizz.pojo.dto.RenameDTO;
import top.zhaizz.pojo.entity.ExcelDocument;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.pojo.vo.CreateVo;
import top.zhaizz.pojo.vo.PageQueryVO;
import top.zhaizz.service.ExcelDocumentService;
import top.zhaizz.service.ExcelSheetChunkService;
import top.zhaizz.service.ExcelSheetService;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static top.zhaizz.common.ExcelUtil.buildSheetInfoList;

@Service
@Slf4j
public class ExcelDocumentServiceImpl implements ExcelDocumentService {
    @Autowired
    private ExcelDocumentMapper excelDocumentMapper;
    @Autowired
    private ExcelSheetService excelSheetService;
    @Autowired
    private ExcelSheetChunkService excelSheetChunkService;

    @Value("${excel.upload.path:./upload}")
    private String uploadPath;

    /**
     * 分页查询所有文档
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，包含总条数、总页数、当前页码、每页条数、记录列表
     */
    @Override
    public PageQueryVO list(int pageNum, int pageSize) {
        // 设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（PageHelper 会自动拦截，返回分页后的数据）
        List<ExcelDocument> list = excelDocumentMapper.list();
        // PageInfo 封装了 total, pages, pageNum 等完整分页信息
        PageInfo<ExcelDocument> pageInfo = new PageInfo<>(list);
        // 转换为统一的分页 VO 返回
        return PageQueryVO.builder()
                .total(pageInfo.getTotal())
                .pages(pageInfo.getPages())
                .current(pageInfo.getPageNum())
                .records(pageInfo.getList())
                .build();
    }

    /**
     * 重命名指定文档
     *
     * @param id         文档id
     * @param renameDTO  重命名 DTO
     */
    @Override
    public void rename(long id, RenameDTO renameDTO) {
        ExcelDocument excelDocument = ExcelDocument.builder().id(id).name(renameDTO.getName()).build();
        excelDocumentMapper.updateById(excelDocument);
    }

    /**
     * 删除文档主记录
     *
     * @param id 文档ID
     */
    @Override
    public void delete(long id) {
        // 删除硬盘中存储的文档文件
        // 查询出文档实体，以便获取 filePath
        ExcelDocument existDoc = excelDocumentMapper.getById(id);
        if (existDoc != null && existDoc.getFilePath() != null) {
            try {
                //
                java.nio.file.Path path = java.nio.file.Paths.get(existDoc.getFilePath());
                java.nio.file.Files.deleteIfExists(path);
            } catch (Exception e) {
                // 如果文件已被删除或占用，忽略报错，不影响数据库数据的清理
            }
        }

        // 更新文档主记录状态为已删除
        ExcelDocument excelDocument = ExcelDocument.builder().id(id).status(3).build();
        excelDocumentMapper.updateById(excelDocument);
    }

    /**
     * 删除文档（含 Sheet 和 Chunk），带事务控制
     *
     * @param id 文档ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(long id) {
        delete(id);                  // 删除文档主记录
        excelSheetService.delete(id); // 软删除文档 Sheet
        excelSheetChunkService.delete(id); // 物理删除 Chunk
    }

    /**
     * 创建文档记录（初始化时不含 sheetCount/sheetNames，解析完成后调用 updateSheetMeta 更新）
     *
     * @param file 上传的文件
     */
    @Override
    public CreateVo create(MultipartFile file) {
        // ===== 文件校验 =====
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        if (!originalName.endsWith(".xlsx") && !originalName.endsWith(".xls")) {
            throw new IllegalArgumentException("仅支持 Excel 文件（.xlsx / .xls）");
        }

        Path filePath = null;
        try {
            // 保存原始文件
            String savedName = UUID.randomUUID() + "_" + originalName;
            Path dir = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            filePath = dir.resolve(savedName);
            file.transferTo(filePath.toFile());

            // 插入文档主记录（获取 documentId 供分块写入时使用）
            // createTime / updateTime 由 @AutoFill(INSERT) AOP 自动填充
            ExcelDocument excelDocument = ExcelDocument.builder()
                    .name(originalName)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .creatorId("demo-user")
                    .status(1)
                    .version(1L)
                    .sheetCount(0)
                    .build();

            excelDocumentMapper.insert(excelDocument);

            // 流式解析 + 分块写库（核心改造点）
            List<ExcelSheet> sheets = new ArrayList<>();
            try (FileInputStream fis = new FileInputStream(filePath.toFile());
                 Workbook workbook = WorkbookFactory.create(fis)) {
                int sheetTotal = workbook.getNumberOfSheets();
                log.info("开始解析文档 [{}], 共 {} 个 Sheet", excelDocument.getName(), sheetTotal);

                for (int si = 0; si < sheetTotal; si++) {
                    Sheet sheet = workbook.getSheetAt(si);
                    log.info("  → 解析 Sheet[{}]: {}", si, sheet.getSheetName());

                    ExcelSheet sheetEntity = excelSheetService.saveSheetMeta(sheet, excelDocument, si);
                    sheets.add(sheetEntity);

                    excelSheetChunkService.saveSheetChunks(sheet, workbook, sheetEntity);
                }
            }
            log.info("文档 [{}] 解析完成，共 {} 个 Sheet", excelDocument.getName(), sheets.size());

            // 更新文档的 sheetCount / sheetNames
            List<String> sheetNames = new ArrayList<>();
            for (ExcelSheet s : sheets) {
                sheetNames.add(s.getSheetName());
            }
            updateSheetMeta(excelDocument.getId(), sheets.size(),
                    JSONArray.toJSONString(sheetNames));

            // 组装响应（只返回元信息，不返回 celldata）
            List<Map<String, Object>> sheetInfoList = buildSheetInfoList(sheets);

            return CreateVo.builder()
                    .documentId(excelDocument.getId())
                    .name(excelDocument.getName())
                    .sheetCount(sheets.size())
                    .sheets(sheetInfoList)
                    .build();

        } catch (Exception e) {
            log.error("文件保存失败", e);
            // 清理已保存的脏文件
            if (filePath != null) {
                try {
                    Files.deleteIfExists(filePath);
                } catch (Exception ex) {
                    log.warn("清理脏文件失败: {}", ex.getMessage());
                }
            }
            throw new RuntimeException("文件处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析完成后更新文档的 Sheet 数量和名称列表
     *
     * @param id         文档 ID
     * @param sheetCount Sheet 数量
     * @param sheetNames Sheet 名称列表 JSON 字符串（如 ["Sheet1","Sheet2"]）
     */
    @Override
    public void updateSheetMeta(Long id, int sheetCount, String sheetNames) {
        // updateTime 由 @AutoFill(UPDATE) AOP 自动填充
        ExcelDocument excelDocument = ExcelDocument.builder()
                .id(id)
                .sheetCount(sheetCount)
                .sheetNames(sheetNames)
                .build();
        excelDocumentMapper.updateById(excelDocument);
    }
}
