package com.damai.damaiticket.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;

public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // ✅ 放行预检请求（CORS 必需）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // ✅ 放行上传接口与静态图片访问
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/upload/") || uri.startsWith("/uploads/")) {
            return true;
        }

        // ✅ 只校验 /api/admin/**（配合 WebConfig）
        String flag = request.getHeader("X-ADMIN");
        if ("1".equals(flag)) {
            return true;
        }

        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"无管理员权限\"}");
        return false;
    }
}
