package top.zhaizz.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.zhaizz.pojo.entity.ExcelDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档列表查询返回类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageQueryVO {
    /** 总记录数 */
    private Long total;
    /** 总页数 */
    private Integer pages;
    /** 当前页码 */
    private Integer current;
    /** 当前页记录列表 */
    private List<ExcelDocument> records = new ArrayList<>();
}
