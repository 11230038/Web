package com.example.end.controller;

import com.example.end.audit.Log;
import com.example.end.auth.AccessService;
import com.example.end.config.UserRoleConfig;
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
    private final AccessService accessService;

    public TaskLogController(TaskLogService taskLogService, AccessService accessService) {
        this.taskLogService = taskLogService;
        this.accessService = accessService;
    }

    @Log("新增进度记录")
    @PostMapping
    public Result<TaskLog> add(@RequestBody TaskLog taskLog) {
        if (taskLog.getOperatorId() == null) {
            taskLog.setOperatorId(accessService.currentUserId());
        }
        return Result.success(taskLogService.add(taskLog));
    }

    @Log("删除进度记录")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Integer id) {
        TaskLog existingLog = taskLogService.getById(id);
        if (existingLog == null) {
            return Result.error(404, "task log not found");
        }
        if (!accessService.isManager() && !accessService.isCurrentUser(existingLog.getOperatorId())) {
            return forbidden();
        }
        boolean deleted = taskLogService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "task log not found");
        }
        return Result.success(true);
    }

    @Log("更新进度记录")
    @PutMapping
    public Result<Boolean> updateById(@RequestBody TaskLog taskLog) {
        TaskLog existingLog = taskLogService.getById(taskLog.getId());
        if (existingLog == null) {
            return Result.error(404, "task log not found");
        }

        TaskLog targetLog = taskLog;
        if (!accessService.isManager()) {
            if (!accessService.isCurrentUser(existingLog.getOperatorId())) {
                return forbidden();
            }
            targetLog = copyForSelfUpdate(existingLog, taskLog);
        }
        boolean updated = taskLogService.updateById(targetLog);
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
        var currentUser = accessService.currentUser();
        if (currentUser != null && currentUser.getRole() != null) {
            if (currentUser.getRole() == UserRoleConfig.ROLE_PROJECT_OWNER) {
                return Result.success(taskLogService.getAllByOwnerId(accessService.currentUserId()));
            }
            if (currentUser.getRole() == UserRoleConfig.ROLE_EMPLOYEE) {
                return Result.success(taskLogService.getAllByOperatorId(accessService.currentUserId()));
            }
        }
        return Result.success(taskLogService.getAll());
    }

    private <T> Result<T> forbidden() {
        return Result.error(403, "forbidden");
    }

    private TaskLog copyForSelfUpdate(TaskLog existingLog, TaskLog incomingLog) {
        TaskLog targetLog = new TaskLog();
        targetLog.setId(existingLog.getId());
        targetLog.setOperatorId(existingLog.getOperatorId());
        targetLog.setTaskId(incomingLog.getTaskId() == null ? existingLog.getTaskId() : incomingLog.getTaskId());
        targetLog.setProgressPercent(incomingLog.getProgressPercent() == null ? existingLog.getProgressPercent() : incomingLog.getProgressPercent());
        targetLog.setContent(incomingLog.getContent() == null ? existingLog.getContent() : incomingLog.getContent());
        return targetLog;
    }
}
