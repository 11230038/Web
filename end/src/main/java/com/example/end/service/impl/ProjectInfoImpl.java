package com.example.end.service.impl;

import com.example.end.mapper.ProjectInfoMapper;
import com.example.end.pojo.ProjectInfo;
import com.example.end.service.ProjectInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectInfoImpl implements ProjectInfoService {

    private final ProjectInfoMapper projectInfoMapper;

    public ProjectInfoImpl(ProjectInfoMapper projectInfoMapper) {
        this.projectInfoMapper = projectInfoMapper;
    }

    @Override
    public ProjectInfo add(ProjectInfo projectInfo) {
        projectInfoMapper.insert(projectInfo);
        return projectInfo;
    }

    @Override
    public boolean deleteById(Long id) {
        return projectInfoMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateById(ProjectInfo projectInfo) {
        return projectInfoMapper.updateById(projectInfo) > 0;
    }

    @Override
    public ProjectInfo getById(Long id) {
        return projectInfoMapper.selectById(id);
    }

    @Override
    public List<ProjectInfo> getAll() {
        return projectInfoMapper.selectAll();
    }
}
