package com.example.end.auth;

import com.example.end.pojo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<AuthLoginResponse> login(@RequestBody AuthLoginRequest request) {
        try {
            return Result.success(authService.login(request));
        } catch (IllegalArgumentException e) {
            return Result.error(401, e.getMessage());
        }
    }
}
