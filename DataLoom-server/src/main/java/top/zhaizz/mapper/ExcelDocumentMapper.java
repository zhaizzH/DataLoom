package top.zhaizz.mapper;

import org.apache.ibatis.annotations.Select;
import top.zhaizz.pojo.entity.ExcelDocument;

import java.util.List;

public interface ExcelDocumentMapper {

    /**
     * 文档列表查询分页
     *
     * @return 查询结果
     */
    @Select("select * from excel_document where status = 1")
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
     * 更新文档主记录
     *
     * @param excelDocument 文档实体
     */
    void updateById(ExcelDocument excelDocument); // TODO 待添加AOP

    /**
     * 创建文档记录
     *
     * @param excelDocument 文档实体
     */
    void insert(ExcelDocument excelDocument); // TODO 待添加AOP
}
