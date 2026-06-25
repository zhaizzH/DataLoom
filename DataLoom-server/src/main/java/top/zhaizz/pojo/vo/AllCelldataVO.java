package top.zhaizz.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 所有单元格数据视图对象（包含所有 Sheet）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllCelldataVO {
    /**
     * 所属 Sheet ID
     */
    private Long sheetId;

    /**
     * Luckysheet 兼容的单元格数据数组
     */
    private List<Object> celldata;

    /**
     * 单元格总数（非空单元格）
     */
    private int cellCount;
}
