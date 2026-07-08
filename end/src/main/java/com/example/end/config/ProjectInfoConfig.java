package com.example.end.config;

public final class ProjectInfoConfig {
    public static final int STATUS_NOT_STARTED = 0;

    public static final int STATUS_IN_PROGRESS = 1;

    public static final int STATUS_COMPLETED = 2;

    public static final int PRIORITY_LOW = 0;

    public static final int PRIORITY_MEDIUM = 1;

    public static final int PRIORITY_HIGH = 2;

    private ProjectInfoConfig() {
    }

    public static String getStatusName(Integer status) {
        if (status == null) {
            return null;
        }
        return switch (status) {
            case STATUS_NOT_STARTED -> "未开始";
            case STATUS_IN_PROGRESS -> "进行中";
            case STATUS_COMPLETED -> "已完成";
            default -> "未知状态";
        };
    }

    public static String getPriorityName(Integer priority) {
        if (priority == null) {
            return null;
        }
        return switch (priority) {
            case PRIORITY_LOW -> "低";
            case PRIORITY_MEDIUM -> "中";
            case PRIORITY_HIGH -> "高";
            default -> "未知优先级";
        };
    }
}
