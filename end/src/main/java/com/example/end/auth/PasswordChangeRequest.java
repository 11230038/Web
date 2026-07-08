package com.example.end.auth;

import lombok.Data;

@Data
public class PasswordChangeRequest {
    private String oldPassword;

    private String newPassword;
}
