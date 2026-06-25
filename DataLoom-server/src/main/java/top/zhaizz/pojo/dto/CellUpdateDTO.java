package top.zhaizz.pojo.dto;

import lombok.Data;

/**
 * 单元格批量更新 DTO
 */
@Data
public class CellUpdateDTO {
    /** Sheet ID */
    private Long sheetId;
    /** 行号 */
    private Integer r;
    /** 列号 */
    private Integer c;
    /** 单元格值 */
    private Object v;
}
