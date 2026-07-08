package com.example.end.agent;

import lombok.Data;

@Data
public class TaskSuggestionRequest {
    private String projectName;

    private String taskTitle;

    private String description;
}
