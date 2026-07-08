package com.example.end.util;

public final class CurrentHolder {
    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();

    private CurrentHolder() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void clear() {
        CURRENT_ID.remove();
    }
}
