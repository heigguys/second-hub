package com.nie.secondhub.security;

import com.nie.secondhub.common.context.LoginUser;
import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.exception.BizException;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/api/user/auth/") || path.startsWith("/api/admin/auth/")
                || path.startsWith("/api/user/public/")
                || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
                || path.startsWith("/doc.html") || path.startsWith("/uploads/")
                || path.startsWith("/error")) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BizException(401, "未登录或令牌缺失");
        }

        Claims claims;
        try {
            claims = jwtTokenUtil.parse(token.substring(7));
        } catch (Exception ex) {
            throw new BizException(401, "令牌无效或已过期");
        }

        Long userId = Long.parseLong(claims.getSubject());
        String role = String.valueOf(claims.get("role"));
        if (path.startsWith("/api/admin/") && !"ADMIN".equals(role)) {
            throw new BizException(403, "无管理员权限");
        }
        if (path.startsWith("/api/user/") && !"USER".equals(role)) {
            throw new BizException(403, "无用户权限");
        }

        LoginUserHolder.set(LoginUser.builder().userId(userId).role(role).build());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginUserHolder.clear();
    }
}
