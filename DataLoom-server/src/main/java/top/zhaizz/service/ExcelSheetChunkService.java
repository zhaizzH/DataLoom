package top.zhaizz.service;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import top.zhaizz.pojo.dto.CellUpdateDTO;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.pojo.vo.AllCelldataVO;

import java.util.List;

public interface ExcelSheetChunkService {
    /**
     * 查询指定文档指定Sheet的所有单元格数据
     *
     * @param id      查询文档id
     * @param sheetId 查询sheetId
     * @return 所有单元格数据
     */
    AllCelldataVO loadAllCelldata(long id, long sheetId);

    /**
     * 批量更新指定文档指定Sheet的所有单元格数据
     *
     * @param id      文档id
     * @param updates 更新数据
     */
    void batchUpdateCells(long id, List<CellUpdateDTO> updates);

    /**
     * 物理删除 Chunk
     *
     * @param id 文档ID,在excel_sheet_chunk表中名为document_id字段
     */
    void delete(long id);

    /**
     * 将一个 Sheet 的所有单元格数据按 CHUNK_SIZE 行分块，批量写入数据库
     *
     * @param sheet sheet对象
     * @param workbook workbook对象
     * @param sheetEntity sheet实体对象
     */
    void saveSheetChunks(Sheet sheet, Workbook workbook, ExcelSheet sheetEntity);
}
