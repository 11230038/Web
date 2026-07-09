package com.example.end.controller;

import com.example.end.audit.Log;
import com.example.end.auth.AccessService;
import com.example.end.config.UserRoleConfig;
import com.example.end.pojo.ProjectInfo;
import com.example.end.pojo.Result;
import com.example.end.service.ProjectInfoService;
import com.example.end.service.SysUserService;
import org.springframework.transaction.annotation.Transactional;
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
    private final SysUserService sysUserService;

    public ProjectInfoController(
            ProjectInfoService projectInfoService,
            AccessService accessService,
            SysUserService sysUserService
    ) {
        this.projectInfoService = projectInfoService;
        this.accessService = accessService;
        this.sysUserService = sysUserService;
    }

    @Log("新增项目")
    @PostMapping
    @Transactional
    public Result<ProjectInfo> add(@RequestBody ProjectInfo projectInfo) {
        var currentUser = accessService.currentUser();
        if (currentUser == null
                || currentUser.getRole() == null
                || (currentUser.getRole() != UserRoleConfig.ROLE_EMPLOYEE
                && currentUser.getRole() != UserRoleConfig.ROLE_PROJECT_OWNER)) {
            return forbidden();
        }
        Long currentUserId = accessService.currentUserId();
        if (currentUserId == null) {
            return forbidden();
        }
        projectInfo.setOwnerId(currentUserId);
        ProjectInfo createdProject = projectInfoService.add(projectInfo);
        if (!sysUserService.updateRoleById(currentUserId, UserRoleConfig.ROLE_PROJECT_OWNER)) {
            throw new IllegalStateException("failed to update user role");
        }
        return Result.success(createdProject);
    }

    @Log("删除项目")
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

    @Log("更新项目")
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
