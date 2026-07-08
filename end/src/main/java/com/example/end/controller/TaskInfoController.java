package com.example.end.controller;

import com.example.end.pojo.Result;
import com.example.end.pojo.TaskInfo;
import com.example.end.service.TaskInfoService;
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
@RequestMapping("/taskInfos")
public class TaskInfoController {

    private final TaskInfoService taskInfoService;

    public TaskInfoController(TaskInfoService taskInfoService) {
        this.taskInfoService = taskInfoService;
    }

    @PostMapping
    public Result<TaskInfo> add(@RequestBody TaskInfo taskInfo) {
        return Result.success(taskInfoService.add(taskInfo));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        boolean deleted = taskInfoService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "task not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody TaskInfo taskInfo) {
        boolean updated = taskInfoService.updateById(taskInfo);
        if (!updated) {
            return Result.error(404, "task not found");
        }
        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<TaskInfo> getById(@PathVariable Long id) {
        TaskInfo taskInfo = taskInfoService.getById(id);
        if (taskInfo == null) {
            return Result.error(404, "task not found");
        }
        return Result.success(taskInfo);
    }

    @GetMapping
    public Result<List<TaskInfo>> getAll() {
        return Result.success(taskInfoService.getAll());
    }
}
