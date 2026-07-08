package com.example.end.controller;

import com.example.end.auth.AccessService;
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
    private final AccessService accessService;

    public TaskInfoController(TaskInfoService taskInfoService, AccessService accessService) {
        this.taskInfoService = taskInfoService;
        this.accessService = accessService;
    }

    @PostMapping
    public Result<TaskInfo> add(@RequestBody TaskInfo taskInfo) {
        if (!accessService.isManager()) {
            return forbidden();
        }
        if (taskInfo.getCreatorId() == null) {
            taskInfo.setCreatorId(accessService.currentUserId());
        }
        return Result.success(taskInfoService.add(taskInfo));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        if (!accessService.isManager()) {
            return forbidden();
        }
        boolean deleted = taskInfoService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "task not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody TaskInfo taskInfo) {
        TaskInfo existingTask = taskInfoService.getById(taskInfo.getId());
        if (existingTask == null) {
            return Result.error(404, "task not found");
        }

        TaskInfo targetTask = taskInfo;
        if (!accessService.isManager()) {
            if (!accessService.isCurrentUser(existingTask.getAssigneeId())) {
                return forbidden();
            }
            targetTask = copyForStatusUpdate(existingTask, taskInfo.getStatus());
        }

        boolean updated = taskInfoService.updateById(targetTask);
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

    private TaskInfo copyForStatusUpdate(TaskInfo existingTask, Integer newStatus) {
        TaskInfo targetTask = new TaskInfo();
        targetTask.setId(existingTask.getId());
        targetTask.setCreatorId(existingTask.getCreatorId());
        targetTask.setAssigneeId(existingTask.getAssigneeId());
        targetTask.setProjectId(existingTask.getProjectId());
        targetTask.setParentId(existingTask.getParentId());
        targetTask.setTitle(existingTask.getTitle());
        targetTask.setDescription(existingTask.getDescription());
        targetTask.setPriority(existingTask.getPriority());
        targetTask.setStatus(newStatus == null ? existingTask.getStatus() : newStatus);
        targetTask.setDueDate(existingTask.getDueDate());
        targetTask.setAiSuggestion(existingTask.getAiSuggestion());
        return targetTask;
    }

    private <T> Result<T> forbidden() {
        return Result.error(403, "forbidden");
    }
}
