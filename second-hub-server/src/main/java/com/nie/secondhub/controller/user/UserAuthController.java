package com.nie.secondhub.controller.user;

import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.dto.user.WxLoginRequest;
import com.nie.secondhub.service.AuthService;
import com.nie.secondhub.vo.LoginVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/user/auth")
public class UserAuthController {

    @Resource
    private AuthService authService;

    @PostMapping("/wx-login")
    public ApiResponse<LoginVO> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        return ApiResponse.success(authService.wxLogin(request));
    }
}
