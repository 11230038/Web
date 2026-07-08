package com.example.end.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthLoginResponse {
    private String token;

    private Long userId;

    private String username;

    private String realName;

    private Integer role;
}
