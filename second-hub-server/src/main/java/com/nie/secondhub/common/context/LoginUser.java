package com.nie.secondhub.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {
    private Long userId;
    private String role;

    public Long getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
