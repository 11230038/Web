package com.example.end.agent;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TaskBreakdownResult {
    private String summary;

    private List<TaskBreakdownItem> items = new ArrayList<>();
}
