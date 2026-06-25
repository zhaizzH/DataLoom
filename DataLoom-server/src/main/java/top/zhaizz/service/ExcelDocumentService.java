package top.zhaizz.service;

import org.springframework.web.multipart.MultipartFile;
import top.zhaizz.pojo.vo.CreateVo;
import top.zhaizz.pojo.vo.PageQueryVO;

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

    /**
     * 删除文档主记录
     *
     * @param id 文档ID
     */
    void delete(long id);

    /**
     * 创建文档记录（初始化时不含 sheetCount/sheetNames，解析完成后调用 updateSheetMeta 更新）
     *
     * @param file 上传的文件
     */
    CreateVo create(MultipartFile file);

    /**
     * 解析完成后更新文档的 Sheet 数量和名称列表
     *
     * @param id      文档 ID
     * @param sheetCount Sheet 数量
     * @param sheetNames Sheet 名称列表 JSON 字符串（如 ["Sheet1","Sheet2"]）
     */
    void updateSheetMeta(Long id, int sheetCount, String sheetNames);
}
