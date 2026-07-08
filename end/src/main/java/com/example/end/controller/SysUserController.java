package com.example.end.controller;

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

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping
    public Result<SysUser> add(@RequestBody SysUser sysUser) {
        return Result.success(sysUserService.add(sysUser));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        boolean deleted = sysUserService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "user not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody SysUser sysUser) {
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
}
