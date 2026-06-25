package top.zhaizz.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import top.zhaizz.pojo.entity.ExcelSheetChunk;

public interface ExcelSheetChunkMapper {
    /**
     * 根据文档id和sheetId查询指定Sheet的所有单元格数据
     * @param id 文档id
     * @param sheetId 查询sheetId
     * @return 返回所有单元格数据
     */
    @Select("select * from excel_sheet_chunk where document_id=#{id} and sheet_id=#{sheetId}")
    ExcelSheetChunk getByIdAndSheetId(long id, long sheetId);

    /**
     * 根据文档ID删除对应chunk
     *
     * @param documentId 文档ID
     */
    @Delete("delete from excel_sheet_chunk where document_id=#{documentId}")
    void deleteByDocumentId(long documentId); // TODO 待添加AOP

    /**
     *
     * @param chunk chunk实体对象
     */
    void insert(ExcelSheetChunk chunk);
}
