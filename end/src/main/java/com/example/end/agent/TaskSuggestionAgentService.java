package com.example.end.agent;

import com.example.end.config.UserRoleConfig;
import com.example.end.pojo.ProjectInfo;
import com.example.end.pojo.SysUser;
import com.example.end.service.ProjectInfoService;
import com.example.end.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskSuggestionAgentService {
    private static final String TASK_SUGGESTION_SYSTEM_PROMPT_PATH = "prompts/task-suggestion-system.txt";
    private static final String TASK_SUGGESTION_USER_PROMPT_PATH = "prompts/task-suggestion-user.txt";
    private static final String PROJECT_BREAKDOWN_SYSTEM_PROMPT_PATH = "prompts/project-breakdown-system.txt";
    private static final String PROJECT_BREAKDOWN_USER_PROMPT_PATH = "prompts/project-breakdown-user.txt";

    private final AiChatClient aiChatClient;
    private final TaskBreakdownJsonParser taskBreakdownJsonParser;
    private final PromptTemplateLoader promptTemplateLoader;
    private final SysUserService sysUserService;
    private final ProjectInfoService projectInfoService;

    public TaskSuggestionAgentService(
            AiChatClient aiChatClient,
            TaskBreakdownJsonParser taskBreakdownJsonParser,
            PromptTemplateLoader promptTemplateLoader,
            SysUserService sysUserService,
            ProjectInfoService projectInfoService
    ) {
        this.aiChatClient = aiChatClient;
        this.taskBreakdownJsonParser = taskBreakdownJsonParser;
        this.promptTemplateLoader = promptTemplateLoader;
        this.sysUserService = sysUserService;
        this.projectInfoService = projectInfoService;
    }

    public TaskSuggestionResponse suggest(TaskSuggestionRequest request) {
        String systemPrompt = promptTemplateLoader.load(TASK_SUGGESTION_SYSTEM_PROMPT_PATH);
        String userPromptTemplate = promptTemplateLoader.load(TASK_SUGGESTION_USER_PROMPT_PATH);
        String userPrompt = userPromptTemplate.formatted(
                safe(request.getProjectName()),
                safe(request.getTaskTitle()),
                safe(request.getDescription())
        );

        String content = aiChatClient.chat(systemPrompt, userPrompt);
        return new TaskSuggestionResponse(limit(content, 300));
    }

    public TaskBreakdownResult decomposeProject(Long projectId, String projectName, String goal, String description) {
        List<AiMemberOption> members = queryMembers(projectId);
        String systemPrompt = promptTemplateLoader.load(PROJECT_BREAKDOWN_SYSTEM_PROMPT_PATH);
        String userPromptTemplate = promptTemplateLoader.load(PROJECT_BREAKDOWN_USER_PROMPT_PATH);
        String userPrompt = userPromptTemplate.formatted(
                safe(projectName),
                safe(goal),
                safe(description),
                buildMemberListText(members)
        );

        String content = aiChatClient.chat(systemPrompt, userPrompt);
        Set<Integer> validAssigneeIds = members.stream()
                .map(AiMemberOption::getId)
                .collect(Collectors.toSet());
        Integer defaultAssigneeId = members.get(0).getId();
        TaskBreakdownResult result = taskBreakdownJsonParser.parse(content, validAssigneeIds, defaultAssigneeId);
        if (result.getItems() == null || result.getItems().isEmpty()) {
            return buildFallbackBreakdown(projectName, goal, description, members, defaultAssigneeId);
        }
        return result;
    }

    private List<AiMemberOption> queryMembers(Long projectId) {
        Long ownerId = resolveProjectOwnerId(projectId);
        List<AiMemberOption> members = sysUserService.getAll().stream()
                .filter(user -> user.getId() != null)
                .filter(user -> isAssignableMember(user, ownerId))
                .sorted(Comparator.comparing((SysUser user) -> isProjectOwner(user, ownerId) ? 0 : 1)
                        .thenComparing(user -> resolveMemberName(user).toLowerCase()))
                .map(this::toMemberOption)
                .toList();
        if (members.isEmpty()) {
            throw new IllegalStateException("no assignable members found in sys_user");
        }
        return members;
    }

    private Long resolveProjectOwnerId(Long projectId) {
        if (projectId == null) {
            return null;
        }
        ProjectInfo projectInfo = projectInfoService.getById(projectId);
        return projectInfo == null ? null : projectInfo.getOwnerId();
    }

    private boolean isAssignableMember(SysUser user, Long ownerId) {
        if (isProjectOwner(user, ownerId)) {
            return true;
        }
        return user.getRole() != null
                && user.getRole() == UserRoleConfig.ROLE_EMPLOYEE;
    }

    private boolean isProjectOwner(SysUser user, Long ownerId) {
        return ownerId != null && ownerId.equals(user.getId());
    }

    private AiMemberOption toMemberOption(SysUser user) {
        return new AiMemberOption(
                Math.toIntExact(user.getId()),
                resolveMemberName(user),
                user.getRole(),
                safeRoleName(user.getRoleName())
        );
    }

    private String resolveMemberName(SysUser user) {
        if (user.getRealName() != null && !user.getRealName().isBlank()) {
            return user.getRealName().trim();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        return "成员" + user.getId();
    }

    private String buildMemberListText(List<AiMemberOption> members) {
        return members.stream()
                .map(member -> member.getId() + " - " + member.getName() + "（" + safeRoleName(member.getRoleName()) + "）")
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String safeRoleName(String roleName) {
        return roleName == null || roleName.isBlank() ? "未知角色" : roleName.trim();
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "无" : value.trim();
    }

    private String limit(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        String normalized = text.trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private TaskBreakdownResult buildFallbackBreakdown(
            String projectName,
            String goal,
            String description,
            List<AiMemberOption> members,
            Integer defaultAssigneeId
    ) {
        TaskBreakdownItem item = new TaskBreakdownItem();
        item.setTitle(buildFallbackTitle(projectName));
        item.setDescription(limit(firstNonBlank(description, goal, projectName), 300));
        item.setPriority("MEDIUM");
        item.setSuggestedDays(3);
        item.setAssigneeId(defaultAssigneeId);

        TaskBreakdownResult result = new TaskBreakdownResult();
        result.setSummary("AI response was empty; generated a fallback breakdown item for "
                + firstNonBlank(projectName, "the project")
                + " and assigned it to "
                + members.get(0).getName()
                + ".");
        result.setItems(List.of(item));
        return result;
    }

    private String buildFallbackTitle(String projectName) {
        String normalizedProjectName = firstNonBlank(projectName, "Project");
        return limit(normalizedProjectName + " initial delivery plan", 120);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }
}
