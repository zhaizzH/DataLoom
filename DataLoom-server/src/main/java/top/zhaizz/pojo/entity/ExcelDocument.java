package top.zhaizz.pojo.entity;

import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Excel 文档主表实体（仅存元数据）
 * 重新设计说明：移除了原来的 {@code sheetDataJson} 字段。
 * 单元格数据现在由 {@link ExcelSheet} + {@link ExcelSheetChunk} 分块管理，
 * 彻底解决十万级数据下单行存储过大的问题。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDocument {

    /** 主键 */
    private Long id;

    /** 文档名称 */
    private String name;

    /** Sheet 数量 */
    private Integer sheetCount;

    /** Sheet 名称列表（JSON 数组，如 ["Sheet1","Sheet2"]） */
    private String sheetNames;

    /** 乐观锁版本号 */
    private Long version;

    /** 状态：1正常 2回收站 3已删除 */
    private Integer status;

    /** 原始文件本地路径（用于备份/重新解析） */
    private String filePath;

    /** 原始文件大小（字节） */
    private Long fileSize;

    /** 创建者 */
    private String creatorId;

    /** 创建时间 */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 更新时间 */
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

}
