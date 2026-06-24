package top.zhaizz.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zhaizz.mapper.ExcelSheetChunkMapper;
import top.zhaizz.pojo.entity.ExcelSheetChunk;
import top.zhaizz.pojo.vo.AllCelldataVO;
import top.zhaizz.service.ExcelSheetChunkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static top.zhaizz.common.util.parseArrayOrEmpty;
import static top.zhaizz.common.util.parseObjectOrEmpty;

@Service
public class ExcelSheetChunkServiceImpl implements ExcelSheetChunkService {
    @Autowired
    private ExcelSheetChunkMapper excelSheetChunkMapper;

    /**
     * 查询指定文档指定Sheet的所有单元格数据
     *
     * @param id      查询文档id
     * @param sheetId 查询sheetId
     * @return 所有单元格数据
     */
    @Override
    public AllCelldataVO loadAllCelldata(long id, long sheetId) {
        ExcelSheetChunk chunk = excelSheetChunkMapper.getByIdAndSheetId(id, sheetId);
        if (chunk == null) {
            return null;
        }

        List<Object> celldata = new ArrayList<>();
        for (Object o : parseArrayOrEmpty(chunk.getCelldataJson())) {
            JSONObject cell = parseObjectOrEmpty(o.toString());
            if (!cell.isEmpty()) {
                celldata.add(cell);
            }
        }

        return AllCelldataVO.builder()
                .sheetId(chunk.getSheetId())
                .celldata(celldata)
                .cellCount(celldata.size())
                .build();
    }

    /**
     * 批量更新指定文档指定Sheet的所有单元格数据
     *
     * @param id      文档id
     * @param updates 更新数据
     */
    @Override
    public void batchUpdateCells(long id, List<Map<String, Object>> updates) {

    }
}
