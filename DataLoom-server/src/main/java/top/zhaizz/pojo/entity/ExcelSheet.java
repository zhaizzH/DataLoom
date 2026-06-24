package top.zhaizz.pojo.entity;

import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Excel Sheet 实体 — 代表一个文档中的一个 Sheet 页签
 * <p>
 * 与 ExcelDocument 是多对一关系，一个文档含多个 Sheet。
 * 不直接存储单元格数据，数据由 ExcelSheetChunk 分块存储。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelSheet {

    /** 主键 */
    private Long id;

    /** 所属文档 ID */
    private Long documentId;

    /** Sheet 在 Workbook 中的原始下标（0起始） */
    private Integer sheetIndex;

    /** Sheet 名称 */
    private String sheetName;

    /** 该 Sheet 总行数（含表头） */
    private Integer totalRows;

    /** 该 Sheet 总列数 */
    private Integer totalCols;

    /** 分块数量（chunk count） */
    private Integer chunkCount;

    /** 合并单元格配置 JSON（通常较小，直接存这里） */
    private String mergeConfigJson;

    /** 列宽配置 JSON */
    private String columnLenJson;

    /** 行高配置 JSON */
    private String rowLenJson;

    /** Luckysheet 完整 config JSON */
    private String configJson;

    /** Luckysheet 超链接配置 JSON，保存单元格与超链接映射关系 */
    private String hyperlinkConfigJson;

    /** Luckysheet 图片配置 JSON，保存插入的图片信息（Base64等） */
    private String imagesConfigJson;

    /** Luckysheet 条件格式配置 JSON */
    private String conditionFormatJson;

    /** Luckysheet 图表配置 JSON */
    private String chartJson;

    /** 是否为活动 Sheet：1=是，0=否 */
    private Integer active;

    /** 状态：1正常，3已删除 */
    private Integer status;

    /** 创建时间 */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
