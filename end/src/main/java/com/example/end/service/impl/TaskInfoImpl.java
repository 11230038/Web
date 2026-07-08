package com.example.end.service.impl;

import com.example.end.mapper.TaskInfoMapper;
import com.example.end.pojo.TaskInfo;
import com.example.end.service.TaskInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskInfoImpl implements TaskInfoService {

    private final TaskInfoMapper taskInfoMapper;

    public TaskInfoImpl(TaskInfoMapper taskInfoMapper) {
        this.taskInfoMapper = taskInfoMapper;
    }

    @Override
    public TaskInfo add(TaskInfo taskInfo) {
        taskInfoMapper.insert(taskInfo);
        return taskInfo;
    }

    @Override
    public boolean deleteById(Long id) {
        return taskInfoMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateById(TaskInfo taskInfo) {
        return taskInfoMapper.updateById(taskInfo) > 0;
    }

    @Override
    public TaskInfo getById(Long id) {
        return taskInfoMapper.selectById(id);
    }

    @Override
    public List<TaskInfo> getAll() {
        return taskInfoMapper.selectAll();
    }
}
