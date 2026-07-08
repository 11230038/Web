package com.example.end.config;

public final class TaskSummaryConfig {
    public static final int SUMMARY_STAGE = 0;

    public static final int SUMMARY_FINAL = 1;

    private TaskSummaryConfig() {
    }

    public static String getSummaryTypeName(Integer summaryType) {
        if (summaryType == null) {
            return null;
        }
        return switch (summaryType) {
            case SUMMARY_STAGE -> "阶段总结";
            case SUMMARY_FINAL -> "最终总结";
            default -> "未知总结类型";
        };
    }
}
