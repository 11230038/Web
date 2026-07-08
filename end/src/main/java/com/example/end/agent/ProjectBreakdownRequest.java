package com.example.end.agent;

import lombok.Data;

@Data
public class ProjectBreakdownRequest {
    private String projectName;

    private String goal;

    private String description;
}
