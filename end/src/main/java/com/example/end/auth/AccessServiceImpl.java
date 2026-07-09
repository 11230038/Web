package com.example.end.auth;

import com.example.end.config.UserRoleConfig;
import com.example.end.pojo.SysUser;
import com.example.end.service.SysUserService;
import com.example.end.util.CurrentHolder;
import org.springframework.stereotype.Service;

@Service
public class AccessServiceImpl implements AccessService {
    private final SysUserService sysUserService;

    public AccessServiceImpl(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Override
    public Long currentUserId() {
        return CurrentHolder.getCurrentId();
    }

    @Override
    public SysUser currentUser() {
        Long currentUserId = currentUserId();
        if (currentUserId == null) {
            return null;
        }
        return sysUserService.getById(currentUserId);
    }

    @Override
    public boolean isAdmin() {
        SysUser user = currentUser();
        return user != null && user.getRole() != null && user.getRole() == UserRoleConfig.ROLE_ADMIN;
    }

    @Override
    public boolean isManager() {
        SysUser user = currentUser();
        if (user == null || user.getRole() == null) {
            return false;
        }
        return user.getRole() == UserRoleConfig.ROLE_ADMIN
                || user.getRole() == UserRoleConfig.ROLE_PROJECT_OWNER;
    }

    @Override
    public boolean isCurrentUser(Long userId) {
        return userId != null && userId.equals(currentUserId());
    }
}
