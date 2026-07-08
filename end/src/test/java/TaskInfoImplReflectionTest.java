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

class TaskInfoImplReflectionTest {

    @Test
    void addShouldReturnInsertedTask() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));
        Object task = newTask(1L, "task-a");

        Object result = invoke(service, "add", new Class<?>[]{Class.forName("com.example.end.pojo.TaskInfo")}, task);

        assertSame(task, result);
        assertEquals("insert", handler.lastMethodName);
        assertSame(task, handler.lastArgs[0]);
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
        Object task = newTask(2L, "task-b");

        Object result = invoke(service, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskInfo")}, task);

        assertTrue((Boolean) result);
    }

    @Test
    void getByIdShouldReturnMapperResult() throws Exception {
        Recorder handler = new Recorder();
        Object task = newTask(3L, "task-c");
        handler.selectByIdResult = task;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 3L);

        assertSame(task, result);
    }

    @Test
    void getByIdShouldReturnNullWhenTaskMissing() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 100L);

        assertNull(result);
    }

    @Test
    void getAllShouldReturnAllTasks() throws Exception {
        Recorder handler = new Recorder();
        List<Object> tasks = List.of(newTask(1L, "a"), newTask(2L, "b"));
        handler.selectAllResult = tasks;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAll", new Class<?>[0]);

        assertEquals(tasks, result);
    }

    private Object newService(Object mapper) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.impl.TaskInfoImpl");
        Class<?> mapperClass = Class.forName("com.example.end.mapper.TaskInfoMapper");
        Constructor<?> constructor = serviceClass.getConstructor(mapperClass);
        return constructor.newInstance(mapper);
    }

    private Object newMapper(Recorder handler) throws Exception {
        Class<?> mapperClass = Class.forName("com.example.end.mapper.TaskInfoMapper");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{mapperClass}, handler);
    }

    private Object newTask(Long id, String title) throws Exception {
        Class<?> taskClass = Class.forName("com.example.end.pojo.TaskInfo");
        Object task = taskClass.getConstructor().newInstance();
        invoke(task, "setId", new Class<?>[]{Long.class}, id);
        invoke(task, "setCreatorId", new Class<?>[]{Long.class}, 1L);
        invoke(task, "setAssigneeId", new Class<?>[]{Long.class}, 2L);
        invoke(task, "setProjectId", new Class<?>[]{Long.class}, 1L);
        invoke(task, "setParentId", new Class<?>[]{Long.class}, new Object[]{null});
        invoke(task, "setTitle", new Class<?>[]{String.class}, title);
        invoke(task, "setDescription", new Class<?>[]{String.class}, "desc");
        invoke(task, "setPriority", new Class<?>[]{Integer.class}, 1);
        invoke(task, "setStatus", new Class<?>[]{Integer.class}, 1);
        invoke(task, "setDueDate", new Class<?>[]{LocalDate.class}, LocalDate.of(2026, 7, 31));
        invoke(task, "setAiSuggestion", new Class<?>[]{String.class}, "ai");
        return task;
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
