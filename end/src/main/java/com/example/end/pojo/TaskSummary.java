package com.example.end.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskSummary {
    private Long id;

    private Long creatorId;

    private Long projectId;

    private Integer taskId;

    private String summaryType;

    private String content;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
