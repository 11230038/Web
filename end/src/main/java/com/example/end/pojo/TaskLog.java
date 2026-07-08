package com.example.end.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskLog {
    private Integer id;

    private Long operatorId;

    private Long taskId;

    private Integer progressPercent;

    private String content;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
