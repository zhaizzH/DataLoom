package top.zhaizz.service;

import org.apache.poi.ss.usermodel.Sheet;
import top.zhaizz.pojo.entity.ExcelDocument;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.pojo.vo.DocumentDetailVO;

public interface ExcelSheetService {
    /**
     * 查询指定文档详情
     *
     * @param id 查询文档id
     * @return 文档详情
     */
    DocumentDetailVO detail(long id);

    /**
     * 软删除文档Sheet
     *
     * @param id 文档ID
     */
    void delete(long id);

    /**
     * 解析并保存一个 Sheet 的元信息（合并单元格、列宽等）
     *
     * @param sheet      sheet对象
     * @param document   document实体对象
     * @param sheetIndex sheet索引
     * @return 保存的 Sheet 实体对象
     */
    ExcelSheet saveSheetMeta(Sheet sheet, ExcelDocument document, int sheetIndex);
}
