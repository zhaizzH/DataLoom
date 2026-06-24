package top.zhaizz.service;

import top.zhaizz.pojo.VO.ExcelDocumentDetailVo;
import top.zhaizz.pojo.VO.PageQueryVo;

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
    ExcelDocumentDetailVo detail(long id);
}
