package com.example.end.agent;

import com.example.end.auth.AccessService;
import com.example.end.pojo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class TaskSuggestionAgentController {

    private final TaskSuggestionAgentService taskSuggestionAgentService;
    private final AccessService accessService;

    public TaskSuggestionAgentController(TaskSuggestionAgentService taskSuggestionAgentService, AccessService accessService) {
        this.taskSuggestionAgentService = taskSuggestionAgentService;
        this.accessService = accessService;
    }

    @PostMapping("/task-suggestion")
    public Object taskSuggestion(@RequestBody TaskSuggestionRequest request) {
        if (!accessService.isManager()) {
            return Result.error(403, "forbidden");
        }
        return taskSuggestionAgentService.suggest(request);
    }

    @PostMapping("/project-breakdown")
    public Object projectBreakdown(@RequestBody ProjectBreakdownRequest request) {
        if (!accessService.isManager()) {
            return Result.error(403, "forbidden");
        }
        return taskSuggestionAgentService.decomposeProject(
                request.getProjectId(),
                request.getProjectName(),
                request.getGoal(),
                request.getDescription()
        );
    }
}
