package com.example.end.pojo;

import com.example.end.config.TaskSummaryConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskSummary {
    private Long id;

    private Long creatorId;

    private Long projectId;

    private Integer taskId;

    private Integer summaryType;

    private String content;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    public String getSummaryTypeName() {
        return TaskSummaryConfig.getSummaryTypeName(summaryType);
    }
}
