package com.example.end.service.impl;

import com.example.end.mapper.TaskLogMapper;
import com.example.end.pojo.TaskLog;
import com.example.end.service.TaskLogService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskLogImpl implements TaskLogService {

    private final TaskLogMapper taskLogMapper;

    public TaskLogImpl(TaskLogMapper taskLogMapper) {
        this.taskLogMapper = taskLogMapper;
    }

    @Override
    public TaskLog add(TaskLog taskLog) {
        taskLogMapper.insert(taskLog);
        return taskLog;
    }

    @Override
    public boolean deleteById(Integer id) {
        return taskLogMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateById(TaskLog taskLog) {
        return taskLogMapper.updateById(taskLog) > 0;
    }

    @Override
    public TaskLog getById(Integer id) {
        return taskLogMapper.selectById(id);
    }

    @Override
    public List<TaskLog> getAll() {
        return taskLogMapper.selectAll();
    }

    @Override
    public List<TaskLog> getAllByOwnerId(Long ownerId) {
        return taskLogMapper.selectAllByOwnerId(ownerId);
    }

    @Override
    public List<TaskLog> getAllByParticipantId(Long userId) {
        return taskLogMapper.selectAllByParticipantId(userId);
    }

    @Override
    public List<TaskLog> getAllByOperatorId(Long userId) {
        return taskLogMapper.selectAllByOperatorId(userId);
    }
}
