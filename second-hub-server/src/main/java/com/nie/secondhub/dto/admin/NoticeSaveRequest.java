package com.nie.secondhub.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoticeSaveRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String coverUrl;
    private Integer status = 1;
}
