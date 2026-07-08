import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TaskSuggestionAgentServiceDeepSeekIntegrationTest {

    @Test
    void shouldCallDeepSeekAndPrintProjectBreakdown() throws Exception {
        Properties properties = loadAiProperties();
        Assumptions.assumeTrue(hasAiConfig(properties), "DeepSeek config is missing");

        Class<?> aiPropertiesClass = Class.forName("com.example.end.agent.AiProperties");
        Class<?> aiChatClientClass = Class.forName("com.example.end.agent.AiChatClient");
        Class<?> parserClass = Class.forName("com.example.end.agent.TaskBreakdownJsonParser");
        Class<?> loaderClass = Class.forName("com.example.end.agent.PromptTemplateLoader");
        Class<?> serviceClass = Class.forName("com.example.end.agent.TaskSuggestionAgentService");
        Class<?> sysUserServiceClass = Class.forName("com.example.end.service.SysUserService");
        Class<?> projectInfoServiceClass = Class.forName("com.example.end.service.ProjectInfoService");
        Class<?> projectInfoClass = Class.forName("com.example.end.pojo.ProjectInfo");

        Object aiProperties = aiPropertiesClass.getConstructor().newInstance();
        invoke(aiProperties, "setBaseUrl", new Class<?>[]{String.class}, properties.getProperty("ai.base-url"));
        invoke(aiProperties, "setApiKey", new Class<?>[]{String.class}, properties.getProperty("ai.api-key"));
        invoke(aiProperties, "setModel", new Class<?>[]{String.class}, properties.getProperty("ai.model"));

        ObjectMapper objectMapper = new ObjectMapper();
        Object aiChatClient = aiChatClientClass.getConstructor(aiPropertiesClass, ObjectMapper.class)
                .newInstance(aiProperties, objectMapper);
        Object parser = parserClass.getConstructor(ObjectMapper.class).newInstance(objectMapper);
        Object loader = loaderClass.getConstructor().newInstance();
        Object sysUserService = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{sysUserServiceClass},
                (proxy, method, args) -> {
                    if ("getAll".equals(method.getName())) {
                        return List.of(
                                newUser(11L, "olivia", "Olivia", 1),
                                newUser(12L, "liam", "Liam", 2),
                                newUser(13L, "emma", "Emma", 2),
                                newUser(14L, "noah", "Noah", 2)
                        );
                    }
                    if ("getByUsername".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );
        Object projectInfo = projectInfoClass.getConstructor().newInstance();
        invoke(projectInfo, "setId", new Class<?>[]{Long.class}, 21L);
        invoke(projectInfo, "setOwnerId", new Class<?>[]{Long.class}, 11L);
        Object projectInfoService = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{projectInfoServiceClass},
                (proxy, method, args) -> {
                    if ("getById".equals(method.getName())) {
                        return projectInfo;
                    }
                    return null;
                }
        );

        Constructor<?> constructor = serviceClass.getConstructor(
                aiChatClientClass,
                parserClass,
                loaderClass,
                sysUserServiceClass,
                projectInfoServiceClass
        );
        Object service = constructor.newInstance(aiChatClient, parser, loader, sysUserService, projectInfoService);

        Method decomposeProject = serviceClass.getMethod("decomposeProject", Long.class, String.class, String.class, String.class);
        Object result = decomposeProject.invoke(
                service,
                21L,
                "Campus Task Collaboration Platform",
                "Deliver a usable web app for task assignment, progress tracking, and weekly summaries within 4 weeks",
                """
                Build a small team collaboration platform with:
                1. Login and role-based access for admin, project owner, and employee.
                2. Project creation, task assignment, task status updates, and task logs.
                3. AI-assisted task suggestion and project breakdown.
                4. A simple Vue frontend and Spring Boot backend with MySQL.
                5. First milestone in 7 days, final demo in 28 days.
                """
        );

        Object items = invokeGetter(result, "getItems");
        assertNotNull(result);
        assertNotNull(items);
        assertFalse(((List<?>) items).isEmpty(), "DeepSeek returned no breakdown items");

        System.out.println("=== DeepSeek Project Breakdown ===");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
    }

    private Properties loadAiProperties() throws Exception {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application-ai.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        }
        return properties;
    }

    private boolean hasAiConfig(Properties properties) {
        return notBlank(properties.getProperty("ai.base-url"))
                && notBlank(properties.getProperty("ai.api-key"))
                && notBlank(properties.getProperty("ai.model"));
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private Object newUser(Long id, String username, String realName, Integer role) throws Exception {
        Class<?> userClass = Class.forName("com.example.end.pojo.SysUser");
        Object user = userClass.getConstructor().newInstance();
        invoke(user, "setId", new Class<?>[]{Long.class}, id);
        invoke(user, "setUsername", new Class<?>[]{String.class}, username);
        invoke(user, "setRealName", new Class<?>[]{String.class}, realName);
        invoke(user, "setRole", new Class<?>[]{Integer.class}, role);
        return user;
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        return method.invoke(target, args);
    }

    private Object invokeGetter(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }
}
