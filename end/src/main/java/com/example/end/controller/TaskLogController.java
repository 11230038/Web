package com.example.end.controller;

import com.example.end.pojo.Result;
import com.example.end.pojo.TaskLog;
import com.example.end.service.TaskLogService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/taskLogs")
public class TaskLogController {

    private final TaskLogService taskLogService;

    public TaskLogController(TaskLogService taskLogService) {
        this.taskLogService = taskLogService;
    }

    @PostMapping
    public Result<TaskLog> add(@RequestBody TaskLog taskLog) {
        return Result.success(taskLogService.add(taskLog));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Integer id) {
        boolean deleted = taskLogService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "task log not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody TaskLog taskLog) {
        boolean updated = taskLogService.updateById(taskLog);
        if (!updated) {
            return Result.error(404, "task log not found");
        }
        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<TaskLog> getById(@PathVariable Integer id) {
        TaskLog taskLog = taskLogService.getById(id);
        if (taskLog == null) {
            return Result.error(404, "task log not found");
        }
        return Result.success(taskLog);
    }

    @GetMapping
    public Result<List<TaskLog>> getAll() {
        return Result.success(taskLogService.getAll());
    }
}
