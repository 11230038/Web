package com.example.end.auth;

import com.example.end.pojo.SysUser;

public interface AccessService {
    Long currentUserId();

    SysUser currentUser();

    boolean isManager();

    boolean isCurrentUser(Long userId);
}
