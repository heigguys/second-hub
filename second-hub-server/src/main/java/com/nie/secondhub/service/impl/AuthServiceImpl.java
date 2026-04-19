package com.nie.secondhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nie.secondhub.common.enums.RoleType;
import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.dto.admin.AdminLoginRequest;
import com.nie.secondhub.dto.user.WxLoginRequest;
import com.nie.secondhub.entity.AdminUser;
import com.nie.secondhub.entity.User;
import com.nie.secondhub.mapper.AdminUserMapper;
import com.nie.secondhub.mapper.UserMapper;
import com.nie.secondhub.security.JwtTokenUtil;
import com.nie.secondhub.service.AuthService;
import com.nie.secondhub.util.Md5Util;
import com.nie.secondhub.vo.LoginVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginVO wxLogin(WxLoginRequest request) {
        String openid = "wx_" + Md5Util.md5(request.getCode());
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenid, openid));
        LocalDateTime now = LocalDateTime.now();
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(request.getNickname() == null ? "微信用户" : request.getNickname());
            user.setAvatarUrl(request.getAvatarUrl());
            user.setStatus(1);
            user.setPassword(Md5Util.md5("123456"));
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            userMapper.insert(user);
        } else {
            user.setUpdatedAt(now);
            if (request.getNickname() != null && !request.getNickname().isBlank()) {
                user.setNickname(request.getNickname());
            }
            if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
                user.setAvatarUrl(request.getAvatarUrl());
            }
            userMapper.updateById(user);
        }

        String token = jwtTokenUtil.generateToken(user.getId(), RoleType.USER.name());
        return LoginVO.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .role(RoleType.USER.name())
                .token(token)
                .build();
    }

    @Override
    public LoginVO adminLogin(AdminLoginRequest request) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername()));
        if (admin == null || admin.getStatus() != 1) {
            throw new BizException(401, "账号不存在或已禁用");
        }
        if (!Md5Util.md5(request.getPassword()).equals(admin.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        String token = jwtTokenUtil.generateToken(admin.getId(), RoleType.ADMIN.name());
        return LoginVO.builder()
                .id(admin.getId())
                .nickname(admin.getRealName())
                .role(RoleType.ADMIN.name())
                .token(token)
                .build();
    }
}
