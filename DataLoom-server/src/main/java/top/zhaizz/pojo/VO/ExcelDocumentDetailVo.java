package top.zhaizz.pojo.VO;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 文档详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelDocumentDetailVo {
    /** 主键 */
    private Long id;

    /** 文档名称 */
    private String name;

    /** Sheet 数量 */
    private Integer sheetCount;

    /** 乐观锁版本号 */
    private Long version;

    /** Sheet 详情列表 */
    private List<Map<String, Object>> sheets;
}
