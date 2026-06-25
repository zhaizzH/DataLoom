package top.zhaizz.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.zhaizz.common.Result;
import top.zhaizz.pojo.vo.CreateVo;
import top.zhaizz.service.ExcelDocumentService;

/**
 * Excel 文件上传接口
 */
@RestController
@RequestMapping("/api/excel")
@Api("Excel 文件接口")
@Slf4j
public class ExcelFileController {
    @Autowired
    private ExcelDocumentService documentService;

    /**
     * 上传 Excel 文件（支持十万级数据，多 Sheet）
     * <p>
     * 处理流程：
     * <ol>
     *   <li>保存原始文件到本地磁盘（用于备份 / 导出）</li>
     *   <li>流式 POI 解析，按 {@code 1000行/块} 分块写入数据库</li>
     *   <li>返回文档 ID 和 Sheet 元信息列表（不含 celldata，避免响应体过大）</li>
     * </ol>
     *
     * @param file 上传的 Excel 文件（.xlsx / .xls）
     * @return 文档 ID、Sheet 元信息列表
     */
    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            log.info("上传文件参数: {}", file);
            CreateVo result = documentService.create(file);
            if (result == null) {
                return Result.fail("上传失败");
            }
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("上传失败", e);
            return Result.fail("上传失败: " + e.getMessage());
        }
    }


}
