package com.example.end.service;

import com.example.end.pojo.SysUser;

import java.util.List;

public interface SysUserService {

    SysUser add(SysUser sysUser);

    boolean deleteById(Long id);

    boolean updateById(SysUser sysUser);

    SysUser getById(Long id);

    SysUser getByUsername(String username);

    List<SysUser> getAll();
}
