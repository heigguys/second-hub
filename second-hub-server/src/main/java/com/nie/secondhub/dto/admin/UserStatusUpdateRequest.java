package com.nie.secondhub.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    @NotNull(message = "状态不能为空")
    private Integer status;
}
