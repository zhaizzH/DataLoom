package top.zhaizz.service;

import top.zhaizz.pojo.vo.AllCelldataVO;

import java.util.List;
import java.util.Map;

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
    void batchUpdateCells(long id, List<Map<String, Object>> updates);
}
