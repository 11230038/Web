package com.example.end.pojo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProjectInfo {
    private Long id;

    private Long ownerId;

    private String name;

    private String description;

    private Integer priority;

    private Integer status;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;
}
