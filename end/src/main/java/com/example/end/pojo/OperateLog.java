package com.example.end.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperateLog {
    private Long id;

    private Long operateEmpId;

    private LocalDateTime operateTime;

    private String className;

    private String methodName;

    private String methodParams;

    private String returnValue;

    private Long costTime;

    private String operateEmpName;
}
