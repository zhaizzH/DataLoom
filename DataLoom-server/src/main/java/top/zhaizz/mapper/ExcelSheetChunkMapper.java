package top.zhaizz.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.zhaizz.common.annotation.AutoFill;
import top.zhaizz.pojo.entity.ExcelSheetChunk;

import java.util.List;

public interface ExcelSheetChunkMapper {

    /**
     * 根据文档ID删除对应chunk
     *
     * @param documentId 文档ID
     */
    @Delete("delete from excel_sheet_chunk where document_id=#{documentId}")
    void deleteByDocumentId(long documentId);

    /**
     * @param chunk chunk实体对象
     */
    @AutoFill(AutoFill.OperationType.INSERT)
    void insert(ExcelSheetChunk chunk);

    /**
     * 根据文档ID和sheetId查询所有chunk
     *
     * @param documentId 文档ID
     * @param sheetId sheetId
     * @return 返回所有chunk
     */
    @Select("select * from excel_sheet_chunk where document_id=#{documentId} and sheet_id=#{sheetId} order by chunk_index")
    List<ExcelSheetChunk> listByDocumentIdAndSheetId(long documentId, long sheetId);

    /**
     * 更新chunk的celldataJson字段
     *
     * @param chunk chunk实体对象
     */
    @Update("update excel_sheet_chunk set celldata_json=#{celldataJson} where id=#{id}")
    void updateCelldataJson(ExcelSheetChunk chunk);

}
