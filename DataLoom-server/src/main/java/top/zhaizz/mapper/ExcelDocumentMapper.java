package top.zhaizz.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.zhaizz.pojo.entity.ExcelDocument;

import java.util.List;

public interface ExcelDocumentMapper {

    /**
     * 文档列表查询分页
     *
     * @return 查询结果
     */
    @Select("select * from excel_document")
    List<ExcelDocument> list();

    /**
     * 文档详情
     *
     * @param id 文档ID
     * @return 文档详情
     */
    @Select("select * from excel_document where id = #{id}")
    ExcelDocument getById(long id);

    /**
     * 更新重命名指定文档
     *
     * @param excelDocument 文档实体
     */
    @Update("update excel_document set name = #{name} where id = #{id}")
    void updateById(ExcelDocument excelDocument);
}
