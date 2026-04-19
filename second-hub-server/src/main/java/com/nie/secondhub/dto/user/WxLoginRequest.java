package com.nie.secondhub.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginRequest {
    @NotBlank(message = "code不能为空")
    private String code;
    private String nickname;
    private String avatarUrl;
}
