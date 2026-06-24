package top.zhaizz.mapper;

import org.apache.ibatis.annotations.Select;
import top.zhaizz.pojo.entity.ExcelSheetChunk;

import java.util.List;

public interface ExcelSheetChunkMapper {
    /**
     * 根据文档 ID 和 Sheet ID 查询所有分块
     * @param id 文档 ID
     * @param sheetId Sheet ID
     * @return 所有分块列表
     */
    @Select("select * from excel_sheet_chunk where document_id=#{id} and sheet_id=#{sheetId}")
    List<ExcelSheetChunk> listSheetsChunkById(long id, long sheetId);
}
