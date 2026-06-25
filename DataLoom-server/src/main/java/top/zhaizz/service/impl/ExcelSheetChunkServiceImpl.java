package top.zhaizz.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.zhaizz.mapper.ExcelSheetChunkMapper;
import top.zhaizz.mapper.ExcelSheetMapper;
import top.zhaizz.pojo.entity.ExcelSheet;
import top.zhaizz.pojo.entity.ExcelSheetChunk;
import top.zhaizz.pojo.vo.AllCelldataVO;
import top.zhaizz.service.ExcelSheetChunkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static top.zhaizz.common.util.*;

@Service
@Slf4j
public class ExcelSheetChunkServiceImpl implements ExcelSheetChunkService {
    /**
     * 每块存储的最大行数（可按实际数据密度调整）
     */
    public static final int CHUNK_SIZE = 1000;
    @Autowired
    private ExcelSheetMapper excelSheetMapper;
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

    /**
     * 物理删除 Chunk
     *
     * @param id 文档ID,在excel_sheet_chunk表中名为document_id字段
     */
    @Override
    public void delete(long id) {
        // 根据document_id删除对应chunk
        excelSheetChunkMapper.deleteByDocumentId(id);
    }

    @Override
    public void saveSheetChunks(Sheet sheet, Workbook workbook, ExcelSheet sheetEntity) {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        int lastRowNum = sheet.getLastRowNum();

        // 当前块正在积累的 celldata 条目
        List<JSONObject> buffer = new ArrayList<>(CHUNK_SIZE * 10);
        int chunkIndex = 0;
        int chunkStartRow = 0;
        int chunkEndRow;

        for (int rowIdx = 0; rowIdx <= lastRowNum; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) continue;

            for (Cell cell : row) {
                JSONObject vObj = buildCellValue(cell, evaluator);
                if (vObj != null) {
                    JSONObject cellItem = new JSONObject();
                    cellItem.put("r", cell.getRowIndex());
                    cellItem.put("c", cell.getColumnIndex());
                    cellItem.put("v", vObj);
                    buffer.add(cellItem);
                }
            }

            chunkEndRow = rowIdx;

            // 达到分块大小时，将 buffer 写入数据库
            boolean isChunkFull = (rowIdx - chunkStartRow + 1) >= CHUNK_SIZE;
            boolean isLastRow = (rowIdx == lastRowNum);

            if ((isChunkFull || isLastRow) && !buffer.isEmpty()) {
                ExcelSheetChunk excelSheetChunk = new ExcelSheetChunk();
                excelSheetChunk.setDocumentId(sheetEntity.getDocumentId());
                excelSheetChunk.setSheetId(sheetEntity.getId());
                excelSheetChunk.setChunkIndex(chunkIndex);
                excelSheetChunk.setRowStart(chunkStartRow);
                excelSheetChunk.setRowEnd(chunkEndRow);
                excelSheetChunk.setCelldataJson(JSONArray.toJSONString(buffer));
                excelSheetChunkMapper.insert(excelSheetChunk);

                log.debug("Chunk[{}] 已写入: rows {}-{}, cellCount={}", chunkIndex, chunkStartRow, chunkEndRow, buffer.size());

                // 重置缓冲区
                buffer.clear();
                chunkIndex++;
                chunkStartRow = rowIdx + 1;
            } else if (isChunkFull) {
                // buffer 为空但行数达到阈值，只推进 chunkStartRow
                chunkStartRow = rowIdx + 1;
            }
        }

        // 更新 chunkCount
        ExcelSheet update = new ExcelSheet();
        update.setDocumentId(sheetEntity.getId());
        update.setChunkCount(chunkIndex);
        excelSheetMapper.updateByDocumentId(update);
        sheetEntity.setChunkCount(chunkIndex);

        log.info("    Sheet [{}] 分块完成，共 {} 块", sheetEntity.getSheetName(), chunkIndex);
    }
}
