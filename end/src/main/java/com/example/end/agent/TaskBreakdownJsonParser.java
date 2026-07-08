package com.example.end.agent;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class TaskBreakdownJsonParser {
    private static final Set<Integer> ALLOWED_ASSIGNEE_IDS = Set.of(1, 2, 3, 4);
    private static final int DEFAULT_ASSIGNEE_ID = 1;

    private final ObjectMapper objectMapper;

    public TaskBreakdownJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TaskBreakdownResult parse(String aiContent) {
        return parse(aiContent, ALLOWED_ASSIGNEE_IDS, DEFAULT_ASSIGNEE_ID);
    }

    public TaskBreakdownResult parse(String aiContent, Set<Integer> allowedAssigneeIds, Integer defaultAssigneeId) {
        try {
            TaskBreakdownResult result = readResult(extractJson(aiContent));
            if (result.getItems() == null) {
                result.setItems(Collections.emptyList());
            }
            for (TaskBreakdownItem item : result.getItems()) {
                if (item.getAssigneeId() == null || !allowedAssigneeIds.contains(item.getAssigneeId())) {
                    item.setAssigneeId(defaultAssigneeId);
                }
            }
            return result;
        } catch (RuntimeException e) {
            throw new IllegalStateException("failed to parse ai json", e);
        }
    }

    private TaskBreakdownResult readResult(String json) throws RuntimeException {
        JsonNode root = objectMapper.readTree(json);
        if (root.isArray()) {
            TaskBreakdownResult result = new TaskBreakdownResult();
            List<TaskBreakdownItem> items = objectMapper.convertValue(root, new TypeReference<List<TaskBreakdownItem>>() {
            });
            result.setItems(items);
            return result;
        }
        return objectMapper.treeToValue(root, TaskBreakdownResult.class);
    }

    private String extractJson(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstLineEnd >= 0 && lastFence > firstLineEnd) {
                return trimmed.substring(firstLineEnd + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}
