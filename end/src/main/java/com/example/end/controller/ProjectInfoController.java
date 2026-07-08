package com.example.end.controller;

import com.example.end.auth.AccessService;
import com.example.end.pojo.ProjectInfo;
import com.example.end.pojo.Result;
import com.example.end.service.ProjectInfoService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projectInfos")
public class ProjectInfoController {

    private final ProjectInfoService projectInfoService;
    private final AccessService accessService;

    public ProjectInfoController(ProjectInfoService projectInfoService, AccessService accessService) {
        this.projectInfoService = projectInfoService;
        this.accessService = accessService;
    }

    @PostMapping
    public Result<ProjectInfo> add(@RequestBody ProjectInfo projectInfo) {
        if (!accessService.isManager()) {
            return forbidden();
        }
        if (projectInfo.getOwnerId() == null) {
            projectInfo.setOwnerId(accessService.currentUserId());
        }
        return Result.success(projectInfoService.add(projectInfo));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteById(@PathVariable Long id) {
        if (!accessService.isManager()) {
            return forbidden();
        }
        boolean deleted = projectInfoService.deleteById(id);
        if (!deleted) {
            return Result.error(404, "project not found");
        }
        return Result.success(true);
    }

    @PutMapping
    public Result<Boolean> updateById(@RequestBody ProjectInfo projectInfo) {
        if (!accessService.isManager()) {
            return forbidden();
        }
        boolean updated = projectInfoService.updateById(projectInfo);
        if (!updated) {
            return Result.error(404, "project not found");
        }
        return Result.success(true);
    }

    @GetMapping("/{id}")
    public Result<ProjectInfo> getById(@PathVariable Long id) {
        ProjectInfo projectInfo = projectInfoService.getById(id);
        if (projectInfo == null) {
            return Result.error(404, "project not found");
        }
        return Result.success(projectInfo);
    }

    @GetMapping
    public Result<List<ProjectInfo>> getAll() {
        return Result.success(projectInfoService.getAll());
    }

    private <T> Result<T> forbidden() {
        return Result.error(403, "forbidden");
    }
}
