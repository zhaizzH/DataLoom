package top.zhaizz.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 上传文件视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVo{
    /** 文档唯一 ID */
    private long documentId;
    /** 原始文件名 */
    private String name;
    /** Sheet 数量 */
    private Integer sheetCount;
    /** Sheet 列表 */
    private List<Map<String, Object>> sheets;

}
