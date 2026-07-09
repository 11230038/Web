package com.example.end.controller;

import com.example.end.auth.AccessService;
import com.example.end.auth.PasswordChangeRequest;
import com.example.end.auth.PasswordUtil;
import com.example.end.config.UserRoleConfig;
import com.example.end.pojo.Result;
import com.example.end.pojo.SysUser;
import com.example.end.service.SysUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sysUsers")
public class SysUserController {

    private final SysUserService sysUserService;
    private final AccessService accessService;

    public SysUserController(SysUserService sysUserService, AccessService accessService) {
        this.sysUserService = sysUserService;
        this.accessService = accessService;
    }

    @PostMapping
    public Result<SysUser> add(@RequestBody SysUser sysUser) {
        SysUser currentUser = accessService.currentUser();
        if (currentUser == null
                || currentUser.getRole() == null
                || currentUser.getRole() != UserRoleConfig.ROLE_ADMIN) {
            return forbidden();
        }
        return Result.success(sysUserService.add(sysUser));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        if (!accessService.isManager()) {
            return forbidden();
        }
        boolean deleted = sysUserService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "user not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody SysUser sysUser) {
        SysUser currentUser = accessService.currentUser();
        if (currentUser == null) {
            return Result.error(401, "unauthorized");
        }
        if (!accessService.isManager()) {
            if (sysUser == null || !accessService.isCurrentUser(sysUser.getId())) {
                return forbidden();
            }
            SysUser existingUser = sysUserService.getById(sysUser.getId());
            if (existingUser == null) {
                return Result.error(404, "user not found");
            }
            sysUser.setPassword(existingUser.getPassword());
            sysUser.setRole(existingUser.getRole());
        }
        boolean updated = sysUserService.updateById(sysUser);
        if (!updated) {
            return Result.error(404, "user not found");
        }
        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser sysUser = sysUserService.getById(id);
        if (sysUser == null) {
            return Result.error(404, "user not found");
        }
        return Result.success(sysUser);
    }

    @GetMapping
    public Result<List<SysUser>> getAll() {
        return Result.success(sysUserService.getAll());
    }

    @PostMapping("/change-password")
    public Result<Boolean> changePassword(@RequestBody PasswordChangeRequest request) {
        SysUser currentUser = accessService.currentUser();
        if (currentUser == null) {
            return Result.error(401, "unauthorized");
        }
        if (request == null || request.getOldPassword() == null || request.getNewPassword() == null
                || request.getOldPassword().isBlank() || request.getNewPassword().isBlank()) {
            return Result.error(400, "oldPassword and newPassword are required");
        }
        if (!PasswordUtil.matches(request.getOldPassword(), currentUser.getPassword())) {
            return Result.error(400, "old password is incorrect");
        }
        currentUser.setPassword(PasswordUtil.md5(request.getNewPassword().trim()));
        return Result.success(sysUserService.updateById(currentUser));
    }

    private <T> Result<T> forbidden() {
        return Result.error(403, "forbidden");
    }
}
