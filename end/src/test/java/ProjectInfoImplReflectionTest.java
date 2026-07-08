import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectInfoImplReflectionTest {

    @Test
    void addShouldReturnInsertedProject() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));
        Object project = newProject(1L, "demo");

        Object result = invoke(service, "add", new Class<?>[]{Class.forName("com.example.end.pojo.ProjectInfo")}, project);

        assertSame(project, result);
        assertEquals("insert", handler.lastMethodName);
        assertSame(project, handler.lastArgs[0]);
    }

    @Test
    void deleteByIdShouldReturnTrueWhenRowDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = 1;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "deleteById", new Class<?>[]{Long.class}, 1L);

        assertTrue((Boolean) result);
    }

    @Test
    void deleteByIdShouldReturnFalseWhenNoRowDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = 0;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "deleteById", new Class<?>[]{Long.class}, 9L);

        assertFalse((Boolean) result);
    }

    @Test
    void updateByIdShouldReturnTrueWhenUpdateSucceeds() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = 1;
        Object service = newService(newMapper(handler));
        Object project = newProject(2L, "build");

        Object result = invoke(service, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.ProjectInfo")}, project);

        assertTrue((Boolean) result);
    }

    @Test
    void getByIdShouldReturnMapperResult() throws Exception {
        Recorder handler = new Recorder();
        Object project = newProject(3L, "ship");
        handler.selectByIdResult = project;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 3L);

        assertSame(project, result);
    }

    @Test
    void getByIdShouldReturnNullWhenProjectMissing() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 100L);

        assertNull(result);
    }

    @Test
    void getAllShouldReturnAllProjects() throws Exception {
        Recorder handler = new Recorder();
        List<Object> projects = List.of(newProject(1L, "a"), newProject(2L, "b"));
        handler.selectAllResult = projects;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAll", new Class<?>[0]);

        assertEquals(projects, result);
    }

    private Object newService(Object mapper) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.impl.ProjectInfoImpl");
        Class<?> mapperClass = Class.forName("com.example.end.mapper.ProjectInfoMapper");
        Constructor<?> constructor = serviceClass.getConstructor(mapperClass);
        return constructor.newInstance(mapper);
    }

    private Object newMapper(Recorder handler) throws Exception {
        Class<?> mapperClass = Class.forName("com.example.end.mapper.ProjectInfoMapper");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{mapperClass}, handler);
    }

    private Object newProject(Long id, String name) throws Exception {
        Class<?> projectClass = Class.forName("com.example.end.pojo.ProjectInfo");
        Object project = projectClass.getConstructor().newInstance();
        invoke(project, "setId", new Class<?>[]{Long.class}, id);
        invoke(project, "setOwnerId", new Class<?>[]{Long.class}, 1L);
        invoke(project, "setName", new Class<?>[]{String.class}, name);
        invoke(project, "setDescription", new Class<?>[]{String.class}, "desc");
        invoke(project, "setPriority", new Class<?>[]{Integer.class}, 1);
        invoke(project, "setStatus", new Class<?>[]{Integer.class}, 1);
        invoke(project, "setStartDate", new Class<?>[]{LocalDate.class}, LocalDate.of(2026, 7, 8));
        invoke(project, "setEndDate", new Class<?>[]{LocalDate.class}, LocalDate.of(2026, 7, 31));
        return project;
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        Method method = target.getClass().getMethod(methodName, parameterTypes);
        return method.invoke(target, args);
    }

    private static class Recorder implements InvocationHandler {
        private String lastMethodName;
        private Object[] lastArgs = new Object[0];
        private int deleteResult;
        private int updateResult;
        private Object selectByIdResult;
        private List<Object> selectAllResult = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            lastMethodName = method.getName();
            lastArgs = args == null ? new Object[0] : args;
            return switch (method.getName()) {
                case "insert" -> 1;
                case "deleteById" -> deleteResult;
                case "updateById" -> updateResult;
                case "selectById" -> selectByIdResult;
                case "selectAll" -> selectAllResult;
                default -> null;
            };
        }
    }
}
