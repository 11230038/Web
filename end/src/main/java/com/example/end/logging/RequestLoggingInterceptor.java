package com.example.end.logging;

import com.example.end.util.CurrentHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);
    private static final String REQUEST_START_TIME = RequestLoggingInterceptor.class.getName() + ".startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long durationMs = startTime == null ? -1L : System.currentTimeMillis() - startTime;
        Long currentUserId = CurrentHolder.getCurrentId();

        if (ex != null) {
            log.error(
                    "Request failed: method={}, uri={}, status={}, durationMs={}, userId={}, ip={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    currentUserId,
                    getClientIp(request),
                    ex
            );
            return;
        }

        log.info(
                "Request completed: method={}, uri={}, status={}, durationMs={}, userId={}, ip={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                currentUserId,
                getClientIp(request)
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int separatorIndex = forwarded.indexOf(',');
            return separatorIndex >= 0 ? forwarded.substring(0, separatorIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}
