package com.example.end.controller;

import com.example.end.pojo.Result;
import com.example.end.pojo.TaskSummary;
import com.example.end.service.TaskSummaryService;
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
@RequestMapping("/taskSummaries")
public class TaskSummaryController {

    private final TaskSummaryService taskSummaryService;

    public TaskSummaryController(TaskSummaryService taskSummaryService) {
        this.taskSummaryService = taskSummaryService;
    }

    @PostMapping
    public Result<TaskSummary> add(@RequestBody TaskSummary taskSummary) {
        return Result.success(taskSummaryService.add(taskSummary));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        boolean deleted = taskSummaryService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "task summary not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody TaskSummary taskSummary) {
        boolean updated = taskSummaryService.updateById(taskSummary);
        if (!updated) {
            return Result.error(404, "task summary not found");
        }
        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<TaskSummary> getById(@PathVariable Long id) {
        TaskSummary taskSummary = taskSummaryService.getById(id);
        if (taskSummary == null) {
            return Result.error(404, "task summary not found");
        }
        return Result.success(taskSummary);
    }

    @GetMapping
    public Result<List<TaskSummary>> getAll() {
        return Result.success(taskSummaryService.getAll());
    }
}
