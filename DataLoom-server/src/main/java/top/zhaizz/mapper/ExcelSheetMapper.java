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
}
