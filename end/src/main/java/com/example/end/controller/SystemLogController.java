package com.example.end.controller;

import com.example.end.auth.AccessService;
import com.example.end.config.UserRoleConfig;
import com.example.end.pojo.Result;
import com.example.end.pojo.SystemLogPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/systemLogs")
public class SystemLogController {
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AccessService accessService;
    private final String logFilePattern;

    public SystemLogController(
            AccessService accessService,
            @Value("${system.log.file-pattern:logs/application.%s.log}") String logFilePattern
    ) {
        this.accessService = accessService;
        this.logFilePattern = logFilePattern;
    }

    @GetMapping("/log1")
    public Result<SystemLogPage> getLog1(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize
    ) throws IOException {
        var currentUser = accessService.currentUser();
        if (currentUser == null || currentUser.getRole() == null || currentUser.getRole() != UserRoleConfig.ROLE_ADMIN) {
            return Result.error(403, "forbidden");
        }

        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        List<String> lines = readLogLines();
        int total = lines.size();
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) safePageSize));
        int normalizedPage = Math.min(safePage, totalPages);
        int startIndex = (normalizedPage - 1) * safePageSize;
        int endIndex = Math.min(startIndex + safePageSize, total);

        SystemLogPage result = new SystemLogPage();
        result.setLogName("log1");
        result.setPage(normalizedPage);
        result.setPageSize(safePageSize);
        result.setTotal(total);
        result.setTotalPages(totalPages);
        result.setItems(total == 0 ? Collections.emptyList() : lines.subList(startIndex, endIndex));
        return Result.success(result);
    }

    private List<String> readLogLines() throws IOException {
        Path logPath = resolveTodayLogPath();
        if (!Files.exists(logPath)) {
            logPath = Path.of("logs", "application.log");
        }
        if (!Files.exists(logPath)) {
            return Collections.emptyList();
        }
        List<String> lines = Files.readAllLines(logPath, StandardCharsets.UTF_8);
        Collections.reverse(lines);
        return lines;
    }

    private Path resolveTodayLogPath() {
        String today = LocalDate.now().format(LOG_DATE_FORMATTER);
        return Path.of(String.format(logFilePattern, today));
    }
}
