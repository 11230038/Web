package com.example.end.controller;

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
    public SysUser add(@RequestBody SysUser sysUser) {
        return sysUserService.add(sysUser);
    }

    @DeleteMapping("/{id}")
    public boolean deleteById(@PathVariable Long id) {
        return sysUserService.deleteById(id);
    }

    @PutMapping
    public boolean updateById(@RequestBody SysUser sysUser) {
        return sysUserService.updateById(sysUser);
    }

    @GetMapping("/{id}")
    public SysUser getById(@PathVariable Long id) {
        return sysUserService.getById(id);
    }

    @GetMapping
    public List<SysUser> getAll() {
        return sysUserService.getAll();
    }
}
