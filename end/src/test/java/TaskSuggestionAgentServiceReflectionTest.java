import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskSuggestionAgentServiceReflectionTest {

    @Test
    void queryMembersShouldIncludeCurrentProjectOwnerAndEmployees() throws Exception {
        Class<?> aiPropertiesClass = Class.forName("com.example.end.agent.AiProperties");
        Class<?> aiChatClientClass = Class.forName("com.example.end.agent.AiChatClient");
        Class<?> parserClass = Class.forName("com.example.end.agent.TaskBreakdownJsonParser");
        Class<?> loaderClass = Class.forName("com.example.end.agent.PromptTemplateLoader");
        Class<?> sysUserServiceClass = Class.forName("com.example.end.service.SysUserService");
        Class<?> projectInfoServiceClass = Class.forName("com.example.end.service.ProjectInfoService");
        Class<?> sysUserClass = Class.forName("com.example.end.pojo.SysUser");
        Class<?> projectInfoClass = Class.forName("com.example.end.pojo.ProjectInfo");
        Class<?> serviceClass = Class.forName("com.example.end.agent.TaskSuggestionAgentService");

        Object aiProperties = aiPropertiesClass.getConstructor().newInstance();
        Object aiChatClient = aiChatClientClass.getConstructor(aiPropertiesClass, ObjectMapper.class)
                .newInstance(aiProperties, new ObjectMapper());
        Object parser = parserClass.getConstructor(ObjectMapper.class).newInstance(new ObjectMapper());
        Object loader = loaderClass.getConstructor().newInstance();

        Object employeeUser = sysUserClass.getConstructor().newInstance();
        sysUserClass.getMethod("setId", Long.class).invoke(employeeUser, 7L);
        sysUserClass.getMethod("setRealName", String.class).invoke(employeeUser, "鐜嬩簲");
        sysUserClass.getMethod("setRole", Integer.class).invoke(employeeUser, 2);

        Object projectOwner = sysUserClass.getConstructor().newInstance();
        sysUserClass.getMethod("setId", Long.class).invoke(projectOwner, 5L);
        sysUserClass.getMethod("setRealName", String.class).invoke(projectOwner, "鏉庡洓");
        sysUserClass.getMethod("setRole", Integer.class).invoke(projectOwner, 1);

        Object adminUser = sysUserClass.getConstructor().newInstance();
        sysUserClass.getMethod("setId", Long.class).invoke(adminUser, 1L);
        sysUserClass.getMethod("setRealName", String.class).invoke(adminUser, "绠＄悊鍛?");
        sysUserClass.getMethod("setRole", Integer.class).invoke(adminUser, 0);

        Object otherOwner = sysUserClass.getConstructor().newInstance();
        sysUserClass.getMethod("setId", Long.class).invoke(otherOwner, 9L);
        sysUserClass.getMethod("setRealName", String.class).invoke(otherOwner, "鍏朵粬璐熻矗浜?");
        sysUserClass.getMethod("setRole", Integer.class).invoke(otherOwner, 1);

        Object sysUserService = java.lang.reflect.Proxy.newProxyInstance(
                sysUserServiceClass.getClassLoader(),
                new Class<?>[]{sysUserServiceClass},
                (proxy, method, args) -> {
                    if ("getAll".equals(method.getName())) {
                        return List.of(adminUser, projectOwner, employeeUser, otherOwner);
                    }
                    return null;
                }
        );

        Object projectInfo = projectInfoClass.getConstructor().newInstance();
        projectInfoClass.getMethod("setId", Long.class).invoke(projectInfo, 21L);
        projectInfoClass.getMethod("setOwnerId", Long.class).invoke(projectInfo, 5L);

        Object projectInfoService = java.lang.reflect.Proxy.newProxyInstance(
                projectInfoServiceClass.getClassLoader(),
                new Class<?>[]{projectInfoServiceClass},
                (proxy, method, args) -> {
                    if ("getById".equals(method.getName())) {
                        return projectInfo;
                    }
                    return null;
                }
        );

        Object service = serviceClass.getConstructor(
                        aiChatClientClass,
                        parserClass,
                        loaderClass,
                        sysUserServiceClass,
                        projectInfoServiceClass
                )
                .newInstance(aiChatClient, parser, loader, sysUserService, projectInfoService);

        Method queryMembers = serviceClass.getDeclaredMethod("queryMembers", Long.class);
        queryMembers.setAccessible(true);
        List<?> members = (List<?>) queryMembers.invoke(service, 21L);

        Object firstMember = members.get(0);
        Object secondMember = members.get(1);
        Method getId = firstMember.getClass().getMethod("getId");
        Method getName = firstMember.getClass().getMethod("getName");
        Method getRole = firstMember.getClass().getMethod("getRole");

        assertEquals(2, members.size());
        assertEquals(5, getId.invoke(firstMember));
        assertEquals("鏉庡洓", getName.invoke(firstMember));
        assertEquals(1, getRole.invoke(firstMember));
        assertEquals(7, getId.invoke(secondMember));
    }

    @Test
    void buildMemberListTextShouldContainRoleName() throws Exception {
        Class<?> memberClass = Class.forName("com.example.end.agent.AiMemberOption");
        Class<?> serviceClass = Class.forName("com.example.end.agent.TaskSuggestionAgentService");
        Class<?> aiPropertiesClass = Class.forName("com.example.end.agent.AiProperties");
        Class<?> aiChatClientClass = Class.forName("com.example.end.agent.AiChatClient");
        Class<?> parserClass = Class.forName("com.example.end.agent.TaskBreakdownJsonParser");
        Class<?> loaderClass = Class.forName("com.example.end.agent.PromptTemplateLoader");
        Class<?> sysUserServiceClass = Class.forName("com.example.end.service.SysUserService");
        Class<?> projectInfoServiceClass = Class.forName("com.example.end.service.ProjectInfoService");

        Object aiProperties = aiPropertiesClass.getConstructor().newInstance();
        Object aiChatClient = aiChatClientClass.getConstructor(aiPropertiesClass, ObjectMapper.class)
                .newInstance(aiProperties, new ObjectMapper());
        Object parser = parserClass.getConstructor(ObjectMapper.class).newInstance(new ObjectMapper());
        Object loader = loaderClass.getConstructor().newInstance();
        Object sysUserService = java.lang.reflect.Proxy.newProxyInstance(
                sysUserServiceClass.getClassLoader(),
                new Class<?>[]{sysUserServiceClass},
                (proxy, method, args) -> List.of()
        );
        Object projectInfoService = java.lang.reflect.Proxy.newProxyInstance(
                projectInfoServiceClass.getClassLoader(),
                new Class<?>[]{projectInfoServiceClass},
                (proxy, method, args) -> null
        );

        Object service = serviceClass.getConstructor(
                        aiChatClientClass,
                        parserClass,
                        loaderClass,
                        sysUserServiceClass,
                        projectInfoServiceClass
                )
                .newInstance(aiChatClient, parser, loader, sysUserService, projectInfoService);

        Object member = memberClass.getConstructor(Integer.class, String.class, Integer.class, String.class)
                .newInstance(2, "寮犱笁", 2, "鍛樺伐");
        Method buildMemberListText = serviceClass.getDeclaredMethod("buildMemberListText", List.class);
        buildMemberListText.setAccessible(true);

        String text = (String) buildMemberListText.invoke(service, List.of(member));

        assertTrue(text.contains("寮犱笁"));
        assertTrue(text.contains("鍛樺伐"));
        assertFalse(text.contains("null"));
    }

    @Test
    void promptFilesShouldExist() throws Exception {
        Class<?> loaderClass = Class.forName("com.example.end.agent.PromptTemplateLoader");
        Object loader = loaderClass.getConstructor().newInstance();
        Method load = loaderClass.getMethod("load", String.class);

        String systemPrompt = (String) load.invoke(loader, "prompts/project-breakdown-system.txt");
        String userPrompt = (String) load.invoke(loader, "prompts/project-breakdown-user.txt");

        assertTrue(systemPrompt.contains("assigneeId"));
        assertTrue(userPrompt.contains("id -"));
    }
}
