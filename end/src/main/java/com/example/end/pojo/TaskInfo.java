package com.example.end.pojo;

import com.example.end.config.ProjectInfoConfig;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TaskInfo {
    private Long id;

    private Long creatorId;

    private Long assigneeId;

    private Long projectId;

    private Long parentId;

    private String title;

    private String description;

    private Integer priority;

    private Integer status;

    private LocalDate dueDate;

    private String aiSuggestion;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public String getStatusName() {
        return ProjectInfoConfig.getStatusName(status);
    }

    public String getPriorityName() {
        return ProjectInfoConfig.getPriorityName(priority);
    }
}
