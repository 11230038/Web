package com.example.end.audit;

import com.example.end.auth.AccessService;
import com.example.end.pojo.OperateLog;
import com.example.end.pojo.SysUser;
import com.example.end.service.OperateLogService;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OperationAuditRecorder {
    private static final int MAX_TEXT_LENGTH = 4000;
    private static final List<String> SENSITIVE_KEYS = Arrays.asList("password", "oldPassword", "newPassword");

    private final AccessService accessService;
    private final OperateLogService operateLogService;
    private final ObjectMapper objectMapper;

    public OperationAuditRecorder(
            AccessService accessService,
            OperateLogService operateLogService,
            ObjectMapper objectMapper
    ) {
        this.accessService = accessService;
        this.operateLogService = operateLogService;
        this.objectMapper = objectMapper;
    }

    public void record(String className, String methodName, Object methodParams, Object returnValue, long startTimeMillis) {
        OperateLog log = new OperateLog();
        SysUser currentUser = accessService.currentUser();
        log.setOperateEmpId(currentUser == null ? null : currentUser.getId());
        log.setOperateEmpName(currentUser == null ? null : (currentUser.getRealName() == null || currentUser.getRealName().isBlank()
                ? currentUser.getUsername()
                : currentUser.getRealName()));
        log.setClassName(className);
        log.setMethodName(methodName);
        log.setMethodParams(toJson(methodParams));
        log.setReturnValue(toJson(returnValue));
        log.setCostTime(System.currentTimeMillis() - startTimeMillis);
        operateLogService.add(log);
    }

    private String toJson(Object value) {
        try {
            return truncate(objectMapper.writeValueAsString(sanitizeValue(value)));
        } catch (Exception ex) {
            return truncate(String.valueOf(value));
        }
    }

    private Object sanitizeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Object[] array) {
            return Arrays.stream(array)
                    .map(this::sanitizeValue)
                    .toList();
        }

        Map<String, Object> mapValue = objectMapper.convertValue(value, new TypeReference<>() {
        });
        return sanitizeMap(mapValue);
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> source) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        if (source == null) {
            return sanitized;
        }

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object currentValue = entry.getValue();
            if (isSensitiveKey(key)) {
                sanitized.put(key, "***");
            } else if (currentValue instanceof Map<?, ?> nestedMap) {
                sanitized.put(key, sanitizeMap(castMap(nestedMap)));
            } else if (currentValue instanceof List<?> list) {
                sanitized.put(key, list.stream().map(this::sanitizeCollectionItem).toList());
            } else {
                sanitized.put(key, currentValue);
            }
        }
        return sanitized;
    }

    private Object sanitizeCollectionItem(Object value) {
        if (value instanceof Map<?, ?> nestedMap) {
            return sanitizeMap(castMap(nestedMap));
        }
        return value;
    }

    private boolean isSensitiveKey(String key) {
        return key != null && SENSITIVE_KEYS.stream().anyMatch(item -> item.equalsIgnoreCase(key));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Map<?, ?> value) {
        return (Map<String, Object>) value;
    }

    private String truncate(String value) {
        if (value == null || value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LENGTH);
    }
}
