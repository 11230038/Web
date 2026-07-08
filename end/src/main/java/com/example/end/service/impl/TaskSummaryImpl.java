package com.example.end.service.impl;

import com.example.end.mapper.TaskSummaryMapper;
import com.example.end.pojo.TaskSummary;
import com.example.end.service.TaskSummaryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskSummaryImpl implements TaskSummaryService {

    private final TaskSummaryMapper taskSummaryMapper;

    public TaskSummaryImpl(TaskSummaryMapper taskSummaryMapper) {
        this.taskSummaryMapper = taskSummaryMapper;
    }

    @Override
    public TaskSummary add(TaskSummary taskSummary) {
        taskSummaryMapper.insert(taskSummary);
        return taskSummary;
    }

    @Override
    public boolean deleteById(Long id) {
        return taskSummaryMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateById(TaskSummary taskSummary) {
        return taskSummaryMapper.updateById(taskSummary) > 0;
    }

    @Override
    public TaskSummary getById(Long id) {
        return taskSummaryMapper.selectById(id);
    }

    @Override
    public List<TaskSummary> getAll() {
        return taskSummaryMapper.selectAll();
    }

    @Override
    public List<TaskSummary> getAllByOwnerId(Long ownerId) {
        return taskSummaryMapper.selectAllByOwnerId(ownerId);
    }

    @Override
    public List<TaskSummary> getAllByParticipantId(Long userId) {
        return taskSummaryMapper.selectAllByParticipantId(userId);
    }
}
