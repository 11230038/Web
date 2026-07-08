import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskSuggestionAgentServiceReflectionTest {

    @Test
    void queryMembersShouldFilterAdminRole() throws Exception {
        Class<?> aiPropertiesClass = Class.forName("com.example.end.agent.AiProperties");
        Class<?> aiChatClientClass = Class.forName("com.example.end.agent.AiChatClient");
        Class<?> parserClass = Class.forName("com.example.end.agent.TaskBreakdownJsonParser");
        Class<?> loaderClass = Class.forName("com.example.end.agent.PromptTemplateLoader");
        Class<?> sysUserServiceClass = Class.forName("com.example.end.service.SysUserService");
        Class<?> sysUserClass = Class.forName("com.example.end.pojo.SysUser");
        Class<?> serviceClass = Class.forName("com.example.end.agent.TaskSuggestionAgentService");

        Object aiProperties = aiPropertiesClass.getConstructor().newInstance();
        Object aiChatClient = aiChatClientClass.getConstructor(aiPropertiesClass, ObjectMapper.class)
                .newInstance(aiProperties, new ObjectMapper());
        Object parser = parserClass.getConstructor(ObjectMapper.class).newInstance(new ObjectMapper());
        Object loader = loaderClass.getConstructor().newInstance();

        Object employeeUser = sysUserClass.getConstructor().newInstance();
        sysUserClass.getMethod("setId", Long.class).invoke(employeeUser, 7L);
        sysUserClass.getMethod("setRealName", String.class).invoke(employeeUser, "王五");
        sysUserClass.getMethod("setRole", Integer.class).invoke(employeeUser, 2);

        Object adminUser = sysUserClass.getConstructor().newInstance();
        sysUserClass.getMethod("setId", Long.class).invoke(adminUser, 1L);
        sysUserClass.getMethod("setRealName", String.class).invoke(adminUser, "管理员");
        sysUserClass.getMethod("setRole", Integer.class).invoke(adminUser, 0);

        Object sysUserService = java.lang.reflect.Proxy.newProxyInstance(
                sysUserServiceClass.getClassLoader(),
                new Class<?>[]{sysUserServiceClass},
                (proxy, method, args) -> {
                    if ("getAll".equals(method.getName())) {
                        return List.of(adminUser, employeeUser);
                    }
                    return null;
                }
        );

        Object service = serviceClass.getConstructor(
                        aiChatClientClass,
                        parserClass,
                        loaderClass,
                        sysUserServiceClass
                )
                .newInstance(aiChatClient, parser, loader, sysUserService);

        Method queryMembers = serviceClass.getDeclaredMethod("queryMembers");
        queryMembers.setAccessible(true);
        List<?> members = (List<?>) queryMembers.invoke(service);

        Object member = members.get(0);
        Method getId = member.getClass().getMethod("getId");
        Method getName = member.getClass().getMethod("getName");
        Method getRole = member.getClass().getMethod("getRole");

        assertEquals(1, members.size());
        assertEquals(7, getId.invoke(member));
        assertEquals("王五", getName.invoke(member));
        assertEquals(2, getRole.invoke(member));
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

        Object service = serviceClass.getConstructor(
                        aiChatClientClass,
                        parserClass,
                        loaderClass,
                        sysUserServiceClass
                )
                .newInstance(aiChatClient, parser, loader, sysUserService);

        Object member = memberClass.getConstructor(Integer.class, String.class, Integer.class, String.class)
                .newInstance(2, "张三", 2, "员工");
        Method buildMemberListText = serviceClass.getDeclaredMethod("buildMemberListText", List.class);
        buildMemberListText.setAccessible(true);

        String text = (String) buildMemberListText.invoke(service, List.of(member));

        assertTrue(text.contains("张三"));
        assertTrue(text.contains("员工"));
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
        assertTrue(userPrompt.contains("可选成员名单"));
    }
}
