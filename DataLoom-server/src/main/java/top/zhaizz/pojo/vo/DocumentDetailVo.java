package top.zhaizz.pojo.vo;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 文档详情查询返回类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailVo {
    /** 文档ID */
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
