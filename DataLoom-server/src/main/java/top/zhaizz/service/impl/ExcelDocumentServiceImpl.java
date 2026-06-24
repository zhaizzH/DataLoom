package top.zhaizz.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zhaizz.mapper.ExcelDocumentMapper;
import top.zhaizz.mapper.ExcelSheetChunkMapper;
import top.zhaizz.mapper.ExcelSheetMapper;
import top.zhaizz.pojo.vo.DocumentDetailVo;
import top.zhaizz.pojo.vo.PageQueryVo;
import top.zhaizz.pojo.vo.SheetChunkLoadAllCelldataVO;
import top.zhaizz.pojo.entity.ExcelDocument;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.pojo.entity.ExcelSheetChunk;
import top.zhaizz.service.ExcelDocumentService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelDocumentServiceImpl implements ExcelDocumentService {
    @Autowired
    private ExcelDocumentMapper excelDocumentMapper;
    @Autowired
    private ExcelSheetMapper excelSheetMapper;
    @Autowired
    private ExcelSheetChunkMapper excelSheetChunkMapper;

    /**
     * 文档列表（分页，仅元数据，不含单元格数据）
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，包含总条数、总页数、当前页码、每页条数、记录列表
     */
    @Override
    public PageQueryVo list(int pageNum, int pageSize) {
        // 设置分页参数
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（PageHelper 会自动拦截，返回分页后的数据）
        List<ExcelDocument> list = excelDocumentMapper.list();
        if (list.isEmpty()) {
            return null;
        }
        // PageInfo 封装了 total, pages, pageNum 等完整分页信息
        PageInfo<ExcelDocument> pageInfo = new PageInfo<>(list);
        // 转换为统一的分页 VO 返回
        return PageQueryVo.builder()
                .total(pageInfo.getTotal())
                .pages(pageInfo.getPages())
                .current(pageInfo.getPageNum())
                .records(pageInfo.getList())
                .build();
    }

    /**
     * 文档详情 — 含各 Sheet 元信息（config / chart / images / hyperlink 等），不含 celldata
     *
     * @param id 查询文档id
     * @return 文档详情
     */
    @Override
    public DocumentDetailVo detail(long id) {
        ExcelDocument document = excelDocumentMapper.getById(id);
        if (document == null) {
            return null;
        }

        List<ExcelSheet> sheets = excelSheetMapper.listSheetsByDocumentId(id);

        List<Map<String, Object>> sheetInfoList = new ArrayList<>();
        for (ExcelSheet s : sheets) {
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("sheetId", s.getId());
            info.put("sheetIndex", s.getSheetIndex());
            info.put("sheetName", s.getSheetName());
            info.put("totalRows", s.getTotalRows());
            info.put("totalCols", s.getTotalCols());
            info.put("chunkCount", s.getChunkCount());
            info.put("active", s.getActive());

            // 组装 config（含 merge / columnlen / rowlen）
            JSONObject mergeConfig = parseObjectOrEmpty(s.getMergeConfigJson());
            JSONObject columnLen = parseObjectOrEmpty(s.getColumnLenJson());
            JSONObject rowLen = parseObjectOrEmpty(s.getRowLenJson());
            JSONObject config = parseObjectOrEmpty(s.getConfigJson());
            if (config.isEmpty()) {
                config.put("merge", mergeConfig);
                config.put("columnlen", columnLen);
                config.put("rowlen", rowLen);
            }
            info.put("config", config);
            info.put("mergeConfig", mergeConfig);
            info.put("columnLen", columnLen);
            info.put("rowLen", rowLen);

            // 超链接配置
            info.put("hyperlink", parseObjectOrEmpty(s.getHyperlinkConfigJson()));
            // 图片配置
            info.put("images", parseObjectOrEmpty(s.getImagesConfigJson()));
            // 条件格式配置
            info.put("luckysheet_conditionformat_save", parseArrayOrEmpty(s.getConditionFormatJson()));
            // 图表配置
            info.put("chart", parseArrayOrEmpty(s.getChartJson()));

            sheetInfoList.add(info);
        }

        return DocumentDetailVo.builder()
                .id(document.getId())
                .name(document.getName())
                .sheetCount(document.getSheetCount())
                .version(document.getVersion())
                .sheets(sheetInfoList)
                .build();
    }

    /**
     * 加载指定 Sheet 的全部 celldata（合并所有分块返回）
     *
     * @param id      文档 ID
     * @param sheetId Sheet ID
     * @return 全部 celldata 列表
     */
    @Override
    public SheetChunkLoadAllCelldataVO loadAllCelldata(long id, long sheetId) {
        List<ExcelSheetChunk> excelSheetChunkList = excelSheetChunkMapper.listSheetsChunkById(id, sheetId);
        if (excelSheetChunkList.isEmpty()) {
            return null;
        }

        SheetChunkLoadAllCelldataVO result = null;
        for (ExcelSheetChunk s : excelSheetChunkList) {
            List<Object> celldata = parseArrayOrEmpty(s.getCelldataJson());
            int cellCount = celldata.size();

            result = SheetChunkLoadAllCelldataVO.builder()
                    .sheetId(sheetId)
                    .celldata(celldata)
                    .cellCount(cellCount)
                    .build();
        }
        return result;
    }

    /**
     * 批量更新指定文档的单元格数据
     *
     * @param id      文档 ID
     * @param updates 单元格修改列表：[{"sheetId": 1, "r": 0, "c": 1, "v": {...}}, ...]
     */
    @Override
    public void batchUpdateCells(long id, List<Map<String, Object>> updates) {

    }

    /**
     * 安全解析 JSON 对象，null / 空串 / 解析异常 → 返回空 JSONObject
     */
    private JSONObject parseObjectOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            JSONObject parsed = JSONObject.parseObject(json);
            return parsed == null ? new JSONObject() : parsed;
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    /**
     * 安全解析 JSON 数组，null / 空串 / 解析异常 → 返回空 JSONArray
     */
    private JSONArray parseArrayOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            JSONArray parsed = JSONArray.parseArray(json);
            return parsed == null ? new JSONArray() : parsed;
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }
}
