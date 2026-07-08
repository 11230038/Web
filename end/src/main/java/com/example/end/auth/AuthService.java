package com.example.end.auth;

import com.example.end.pojo.SysUser;
import com.example.end.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    public AuthService(SysUserService sysUserService, JwtProperties jwtProperties, tools.jackson.databind.ObjectMapper objectMapper) {
        this.sysUserService = sysUserService;
        this.jwtUtil = new JwtUtil(jwtProperties, objectMapper);
    }

    public AuthLoginResponse login(AuthLoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new IllegalArgumentException("username and password are required");
        }

        SysUser user = sysUserService.getByUsername(request.getUsername().trim());
        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("username or password is incorrect");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthLoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole()
        );
    }

    public Map<String, Object> validateToken(String token) {
        return jwtUtil.parseAndValidate(token);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
