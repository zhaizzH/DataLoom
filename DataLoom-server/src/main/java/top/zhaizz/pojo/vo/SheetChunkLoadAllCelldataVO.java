package top.zhaizz.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 加载全部单元格数据返回类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SheetChunkLoadAllCelldataVO {
    /** Sheet ID */
    private long sheetId;

    /** Luckysheet 兼容的单元格数据数组 */
    private int cellCount;

    /** 单元格总数（非空单元格）*/
    private List<Object> celldata;
}
