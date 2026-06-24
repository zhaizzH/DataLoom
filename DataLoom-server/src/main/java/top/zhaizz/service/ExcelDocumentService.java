package top.zhaizz.service;

import top.zhaizz.pojo.entity.ExcelSheetChunk;
import top.zhaizz.pojo.vo.AllCelldataVO;
import top.zhaizz.pojo.vo.DocumentDetailVO;
import top.zhaizz.pojo.vo.PageQueryVO;

import java.util.List;
import java.util.Map;

public interface ExcelDocumentService {

    /**
     * 分页查询所有文档
     *
     * @param pageNum  页码，默认 1
     * @param pageSize 每页条数，默认 20
     * @return 分页结果，包含总条数、总页数、当前页码、每页条数、记录列表
     */
    PageQueryVO list(int pageNum, int pageSize);

    /**
     * 重命名指定文档
     *
     * @param id   文档id
     * @param body 新文件名
     */
    void rename(long id, Map<String, String> body);
}
