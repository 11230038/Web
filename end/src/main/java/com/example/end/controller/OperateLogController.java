package com.example.end.controller;

import com.example.end.auth.AccessService;
import com.example.end.pojo.OperateLogPage;
import com.example.end.pojo.Result;
import com.example.end.service.OperateLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/operateLogs")
public class OperateLogController {
    private final OperateLogService operateLogService;
    private final AccessService accessService;

    public OperateLogController(OperateLogService operateLogService, AccessService accessService) {
        this.operateLogService = operateLogService;
        this.accessService = accessService;
    }

    @GetMapping
    public Result<OperateLogPage> getPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        if (!accessService.isAdmin()) {
            return Result.error(403, "forbidden");
        }
        return Result.success(operateLogService.getPage(page, pageSize));
    }
}
