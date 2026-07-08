package com.example.end.service;

import com.example.end.pojo.TaskInfo;

import java.util.List;

public interface TaskInfoService {

    TaskInfo add(TaskInfo taskInfo);

    boolean deleteById(Long id);

    boolean updateById(TaskInfo taskInfo);

    TaskInfo getById(Long id);

    List<TaskInfo> getAll();
}
