package com.example.end.service;

import com.example.end.pojo.TaskLog;

import java.util.List;

public interface TaskLogService {

    TaskLog add(TaskLog taskLog);

    boolean deleteById(Integer id);

    boolean updateById(TaskLog taskLog);

    TaskLog getById(Integer id);

    List<TaskLog> getAll();

    List<TaskLog> getAllByOwnerId(Long ownerId);

    List<TaskLog> getAllByParticipantId(Long userId);

    List<TaskLog> getAllByOperatorId(Long userId);
}
