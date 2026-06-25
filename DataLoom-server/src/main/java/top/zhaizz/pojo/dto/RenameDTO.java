package top.zhaizz.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 文档重命名 DTO
 */
@Data
public class RenameDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
}
