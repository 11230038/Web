import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskSummaryImplReflectionTest {

    @Test
    void addShouldReturnInsertedTaskSummary() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));
        Object taskSummary = newTaskSummary(1L, 0);

        Object result = invoke(service, "add", new Class<?>[]{Class.forName("com.example.end.pojo.TaskSummary")}, taskSummary);

        assertSame(taskSummary, result);
        assertEquals("insert", handler.lastMethodName);
        assertSame(taskSummary, handler.lastArgs[0]);
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
        Object taskSummary = newTaskSummary(2L, 1);

        Object result = invoke(service, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskSummary")}, taskSummary);

        assertTrue((Boolean) result);
    }

    @Test
    void getByIdShouldReturnMapperResult() throws Exception {
        Recorder handler = new Recorder();
        Object taskSummary = newTaskSummary(3L, 1);
        handler.selectByIdResult = taskSummary;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 3L);

        assertSame(taskSummary, result);
    }

    @Test
    void getByIdShouldReturnNullWhenTaskSummaryMissing() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 100L);

        assertNull(result);
    }

    @Test
    void getAllShouldReturnAllTaskSummaries() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskSummaries = List.of(newTaskSummary(1L, 0), newTaskSummary(2L, 1));
        handler.selectAllResult = taskSummaries;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAll", new Class<?>[0]);

        assertEquals(taskSummaries, result);
    }

    @Test
    void getAllByOwnerIdShouldReturnOwnerTaskSummaries() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskSummaries = List.of(newTaskSummary(3L, 0));
        handler.selectAllByOwnerIdResult = taskSummaries;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAllByOwnerId", new Class<?>[]{Long.class}, 9L);

        assertEquals(taskSummaries, result);
        assertEquals("selectAllByOwnerId", handler.lastMethodName);
        assertEquals(9L, handler.lastArgs[0]);
    }

    @Test
    void getAllByParticipantIdShouldReturnParticipantTaskSummaries() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskSummaries = List.of(newTaskSummary(4L, 1));
        handler.selectAllByParticipantIdResult = taskSummaries;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAllByParticipantId", new Class<?>[]{Long.class}, 2L);

        assertEquals(taskSummaries, result);
        assertEquals("selectAllByParticipantId", handler.lastMethodName);
        assertEquals(2L, handler.lastArgs[0]);
    }

    private Object newService(Object mapper) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.impl.TaskSummaryImpl");
        Class<?> mapperClass = Class.forName("com.example.end.mapper.TaskSummaryMapper");
        Constructor<?> constructor = serviceClass.getConstructor(mapperClass);
        return constructor.newInstance(mapper);
    }

    private Object newMapper(Recorder handler) throws Exception {
        Class<?> mapperClass = Class.forName("com.example.end.mapper.TaskSummaryMapper");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{mapperClass}, handler);
    }

    private Object newTaskSummary(Long id, Integer summaryType) throws Exception {
        Class<?> taskSummaryClass = Class.forName("com.example.end.pojo.TaskSummary");
        Object taskSummary = taskSummaryClass.getConstructor().newInstance();
        invoke(taskSummary, "setId", new Class<?>[]{Long.class}, id);
        invoke(taskSummary, "setCreatorId", new Class<?>[]{Long.class}, 1L);
        invoke(taskSummary, "setProjectId", new Class<?>[]{Long.class}, 1L);
        invoke(taskSummary, "setTaskId", new Class<?>[]{Long.class}, 1L);
        invoke(taskSummary, "setSummaryType", new Class<?>[]{Integer.class}, summaryType);
        invoke(taskSummary, "setContent", new Class<?>[]{String.class}, "content");
        return taskSummary;
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
        private List<Object> selectAllByOwnerIdResult = new ArrayList<>();
        private List<Object> selectAllByParticipantIdResult = new ArrayList<>();

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
                case "selectAllByOwnerId" -> selectAllByOwnerIdResult;
                case "selectAllByParticipantId" -> selectAllByParticipantIdResult;
                default -> null;
            };
        }
    }
}
