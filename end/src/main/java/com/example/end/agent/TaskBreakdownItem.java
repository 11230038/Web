package com.example.end.agent;

import lombok.Data;

@Data
public class TaskBreakdownItem {
    private String title;

    private String description;

    private String priority;

    private Integer suggestedDays;

    private Integer assigneeId;
}
