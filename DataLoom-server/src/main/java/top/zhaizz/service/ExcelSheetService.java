package top.zhaizz.service;

import top.zhaizz.pojo.vo.DocumentDetailVO;

public interface ExcelSheetService {
    /**
     * 查询指定文档详情
     *
     * @param id 查询文档id
     * @return 文档详情
     */
    DocumentDetailVO detail(long id);

}
