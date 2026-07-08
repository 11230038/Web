package com.example.end.service;

import com.example.end.pojo.ProjectInfo;

import java.util.List;

public interface ProjectInfoService {

    ProjectInfo add(ProjectInfo projectInfo);

    boolean deleteById(Long id);

    boolean updateById(ProjectInfo projectInfo);

    ProjectInfo getById(Long id);

    List<ProjectInfo> getAll();

    List<ProjectInfo> getAllByOwnerId(Long ownerId);
}
