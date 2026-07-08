package com.example.end.service;

import com.example.end.pojo.TaskSummary;

import java.util.List;

public interface TaskSummaryService {

    TaskSummary add(TaskSummary taskSummary);

    boolean deleteById(Long id);

    boolean updateById(TaskSummary taskSummary);

    TaskSummary getById(Long id);

    List<TaskSummary> getAll();
}
