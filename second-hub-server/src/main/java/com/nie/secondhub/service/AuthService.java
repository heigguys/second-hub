package com.nie.secondhub.service;

import com.nie.secondhub.dto.admin.AdminLoginRequest;
import com.nie.secondhub.dto.user.WxLoginRequest;
import com.nie.secondhub.vo.LoginVO;

public interface AuthService {
    LoginVO wxLogin(WxLoginRequest request);

    LoginVO adminLogin(AdminLoginRequest request);
}
