package com.example.end.config;

public final class UserRoleConfig {
    public static final int ROLE_ADMIN = 0;

    public static final int ROLE_PROJECT_OWNER = 1;

    public static final int ROLE_EMPLOYEE = 2;

    private UserRoleConfig() {
    }

    public static String getRoleName(Integer role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case ROLE_ADMIN -> "管理员";
            case ROLE_PROJECT_OWNER -> "项目负责人";
            case ROLE_EMPLOYEE -> "员工";
            default -> "未知角色";
        };
    }
}
