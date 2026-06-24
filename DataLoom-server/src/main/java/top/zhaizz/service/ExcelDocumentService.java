package top.zhaizz.service;

import top.zhaizz.pojo.vo.DocumentDetailVo;
import top.zhaizz.pojo.vo.PageQueryVo;
import top.zhaizz.pojo.vo.SheetChunkLoadAllCelldataVO;

import java.util.List;
import java.util.Map;

public interface ExcelDocumentService {

    /**
     * 文档列表（分页，仅元数据，不含单元格数据）
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，包含总条数、总页数、当前页码、每页条数、记录列表
     */
    PageQueryVo list(int pageNum, int pageSize);

    /**
     * 文档详情 — 含各 Sheet 元信息（config / chart / images / hyperlink 等），不含 celldata
     *
     * @param id 查询文档id
     * @return 文档详情
     */
    DocumentDetailVo detail(long id);

    /**
     * 加载指定 Sheet 的全部 celldata（合并所有分块返回）
     *
     * @param id 文档 ID
     * @param sheetId Sheet ID
     * @return 全部 celldata 列表
     */
    SheetChunkLoadAllCelldataVO loadAllCelldata(long id, long sheetId);

    /**
     * 批量更新指定文档的单元格数据
     *
     * @param id      文档 ID
     * @param updates 单元格修改列表：[{"sheetId": 1, "r": 0, "c": 1, "v": {...}}, ...]
     */
    void batchUpdateCells(long id, List<Map<String, Object>> updates);
}
