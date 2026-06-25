package top.zhaizz.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import top.zhaizz.pojo.entity.ExcelSheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtil {
    /**
     * 安全解析 JSON 对象，null / 空串 / 解析异常 → 返回空 JSONObject
     */
    public static JSONObject parseObjectOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            JSONObject parsed = JSONObject.parseObject(json);
            return parsed == null ? new JSONObject() : parsed;
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    /**
     * 安全解析 JSON 数组，null / 空串 / 解析异常 → 返回空 JSONArray
     */
    public static JSONArray parseArrayOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            JSONArray parsed = JSONArray.parseArray(json);
            return parsed == null ? new JSONArray() : parsed;
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    /**
     * 将 ExcelSheet 列表转为前端元信息 Map 列表（不含 celldata）
     */
    public static List<Map<String, Object>> buildSheetInfoList(List<ExcelSheet> sheets) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ExcelSheet s : sheets) {
            Map<String, Object> info = new HashMap<>();
            info.put("sheetId", s.getId());
            info.put("sheetIndex", s.getSheetIndex());
            info.put("sheetName", s.getSheetName());
            info.put("totalRows", s.getTotalRows());
            info.put("totalCols", s.getTotalCols());
            info.put("chunkCount", s.getChunkCount());
            info.put("active", s.getActive());
            list.add(info);
        }
        return list;
    }

    /**
     * 根据单元格类型构建 Luckysheet 的 v 对象
     */
    public static JSONObject buildCellValue(Cell cell, FormulaEvaluator evaluator) {
        JSONObject v = new JSONObject();
        JSONObject ct = new JSONObject();

        try {
            switch (cell.getCellType()) {
                case STRING: {
                    String val = cell.getStringCellValue();
                    if (val == null || val.isEmpty()) return null;
                    v.put("v", val);
                    v.put("m", val);
                    ct.put("fa", "General");
                    ct.put("t", "s");
                    break;
                }
                case NUMERIC: {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String dateStr = sdf.format(cell.getDateCellValue());
                        v.put("v", dateStr);
                        v.put("m", dateStr);
                        ct.put("fa", "yyyy-MM-dd");
                        ct.put("t", "d");
                    } else {
                        double num = cell.getNumericCellValue();
                        v.put("v", num);
                        v.put("m", formatNum(num));
                        ct.put("fa", "General");
                        ct.put("t", "n");
                    }
                    break;
                }
                case BOOLEAN: {
                    boolean b = cell.getBooleanCellValue();
                    v.put("v", b);
                    v.put("m", b ? "TRUE" : "FALSE");
                    ct.put("fa", "General");
                    ct.put("t", "b");
                    break;
                }
                case FORMULA: {
                    try {
                        CellValue cv = evaluator.evaluate(cell);
                        v.put("f", "=" + cell.getCellFormula());
                        if (cv != null && cv.getCellType() == CellType.NUMERIC) {
                            v.put("v", cv.getNumberValue());
                            v.put("m", formatNum(cv.getNumberValue()));
                        } else {
                            String sv = cv != null ? cv.getStringValue() : "";
                            v.put("v", sv);
                            v.put("m", sv);
                        }
                        ct.put("fa", "General");
                        ct.put("t", "n");
                    } catch (Exception fe) {
                        // 公式计算失败时降级为字符串
                        String raw = cell.getCellFormula();
                        v.put("v", "=" + raw);
                        v.put("m", "=" + raw);
                        ct.put("fa", "General");
                        ct.put("t", "s");
                    }
                    break;
                }
                case BLANK:
                default:
                    return null;
            }
        } catch (Exception e) {
            // 单个单元格解析异常时跳过，不中断整体解析
            return null;
        }

        v.put("ct", ct);
        return v;
    }

    /**
     * 计算 Sheet 的最大列数
     */
    public static int calcMaxCol(Sheet sheet) {
        int max = 0;
        for (Row row : sheet) {
            if (row.getLastCellNum() > max) {
                max = row.getLastCellNum();
            }
        }
        return max;
    }

    /**
     * 构建合并单元格配置 JSON（Luckysheet merge 格式）
     */
    public static JSONObject buildMergeConfig(Sheet sheet) {
        JSONObject mergeConfig = new JSONObject();
        for (CellRangeAddress range : sheet.getMergedRegions()) {
            String key = range.getFirstRow() + "_" + range.getFirstColumn();
            JSONObject item = new JSONObject();
            item.put("r", range.getFirstRow());
            item.put("c", range.getFirstColumn());
            item.put("rs", range.getLastRow() - range.getFirstRow() + 1);
            item.put("cs", range.getLastColumn() - range.getFirstColumn() + 1);
            mergeConfig.put(key, item);
        }
        return mergeConfig;
    }

    /**
     * 构建列宽配置 JSON（Luckysheet columnlen 格式）
     * 只遍历有数据的列，避免对大量空列调用 getColumnWidthInPixels
     */
    public static JSONObject buildColumnLen(Sheet sheet, int maxCol) {
        JSONObject colWidths = new JSONObject();
        try {
            // 先扫描所有行，收集实际有数据的列索引
            java.util.Set<Integer> dataColumns = new java.util.HashSet<>();
            for (Row row : sheet) {
                if (row == null) continue;
                short lastCell = row.getLastCellNum();
                for (int ci = 0; ci < lastCell; ci++) {
                    Cell cell = row.getCell(ci);
                    if (cell != null && cell.getCellType() != CellType.BLANK) {
                        dataColumns.add(ci);
                    }
                }
            }

            // 只对有数据的列获取宽度，上限取 maxCol 与数据列最大值的较小值
            int effectiveMaxCol = Math.min(maxCol, dataColumns.isEmpty() ? 0
                    : dataColumns.stream().mapToInt(Integer::intValue).max().getAsInt() + 1);
            for (int ci = 0; ci < effectiveMaxCol; ci++) {
                double width = sheet.getColumnWidthInPixels(ci);
                colWidths.put(String.valueOf(ci), (int) Math.max(width, 72));
            }
        } catch (Exception ignored) {
            // 部分 sheet 可能无行数据，忽略
        }
        return colWidths;
    }

    public static JSONObject buildRowLen(Sheet sheet) {
        JSONObject rowHeights = new JSONObject();
        for (Row row : sheet) {
            if (row == null) continue;
            float height = row.getHeightInPoints();
            if (height > 0) {
                rowHeights.put(String.valueOf(row.getRowNum()), Math.round(height / 0.75f));
            }
        }
        return rowHeights;
    }

    public static String formatNum(double num) {
        if (num == (long) num) return String.valueOf((long) num);
        return String.valueOf(num);
    }
}
