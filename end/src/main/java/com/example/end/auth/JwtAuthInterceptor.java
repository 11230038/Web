package com.example.end.auth;

import com.example.end.pojo.Result;
import com.example.end.util.CurrentHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class JwtAuthInterceptor implements org.springframework.web.servlet.HandlerInterceptor {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public JwtAuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        if (requestUri != null && requestUri.contains("login")) {
            return true;
        }

        String token = request.getHeader("token");
        if (token == null || token.isBlank()) {
            writeUnauthorized(response, "missing token");
            return false;
        }

        try {
            Map<String, Object> claims = authService.validateToken(token);
            Number userId = (Number) claims.get("userId");
            if (userId != null) {
                CurrentHolder.setCurrentId(userId.longValue());
            }
            return true;
        } catch (IllegalStateException e) {
            writeUnauthorized(response, e.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentHolder.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}
