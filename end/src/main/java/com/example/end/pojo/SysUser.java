package com.example.end.pojo;

import com.example.end.config.UserRoleConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUser {
    private Long id;

    private String username;

    private String password;

    private String realName;

    private Integer role;

    private String email;

    private String phone;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public String getRoleName() {
        return UserRoleConfig.getRoleName(role);
    }
}
