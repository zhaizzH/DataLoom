package top.zhaizz.mapper;

import org.apache.ibatis.annotations.Select;
import top.zhaizz.pojo.entity.ExcelSheet;

import java.util.List;

public interface ExcelSheetMapper {
    /**
     * 根据文档 ID 查询所有 Sheet
     *
     * @param id 文档 ID
     * @return 所有 Sheet 列表
     */
    @Select("select * from excel_sheet where id = #{id}")
    List<ExcelSheet> listSheetsByDocumentId(long id);

    /**
     * 更新 Sheet 状态
     *
     * @param excelSheet Sheet 实体
     */
    void updateByDocumentId(ExcelSheet excelSheet); // TODO 待添加AOP

    /**
     * 插入 Sheet 实体
     *
     * @param sheetEntity Sheet 实体
     */
    void insert(ExcelSheet sheetEntity);

}
