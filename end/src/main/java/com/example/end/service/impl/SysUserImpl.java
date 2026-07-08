package com.example.end.service.impl;

import com.example.end.mapper.SysUserMapper;
import com.example.end.pojo.SysUser;
import com.example.end.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    public SysUserImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public SysUser add(SysUser sysUser) {
        sysUserMapper.insert(sysUser);
        return sysUser;
    }

    @Override
    public boolean deleteById(Long id) {
        return sysUserMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateById(SysUser sysUser) {
        return sysUserMapper.updateById(sysUser) > 0;
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public List<SysUser> getAll() {
        return sysUserMapper.selectAll();
    }
}
