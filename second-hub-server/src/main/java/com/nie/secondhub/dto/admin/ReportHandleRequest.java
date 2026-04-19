package com.nie.secondhub.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportHandleRequest {
    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
}
