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

class TaskLogImplReflectionTest {

    @Test
    void addShouldReturnInsertedTaskLog() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));
        Object taskLog = newTaskLog(1, "first log");

        Object result = invoke(service, "add", new Class<?>[]{Class.forName("com.example.end.pojo.TaskLog")}, taskLog);

        assertSame(taskLog, result);
        assertEquals("insert", handler.lastMethodName);
        assertSame(taskLog, handler.lastArgs[0]);
    }

    @Test
    void deleteByIdShouldReturnTrueWhenRowDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = 1;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "deleteById", new Class<?>[]{Integer.class}, 1);

        assertTrue((Boolean) result);
    }

    @Test
    void deleteByIdShouldReturnFalseWhenNoRowDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = 0;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "deleteById", new Class<?>[]{Integer.class}, 9);

        assertFalse((Boolean) result);
    }

    @Test
    void updateByIdShouldReturnTrueWhenUpdateSucceeds() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = 1;
        Object service = newService(newMapper(handler));
        Object taskLog = newTaskLog(2, "second log");

        Object result = invoke(service, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskLog")}, taskLog);

        assertTrue((Boolean) result);
    }

    @Test
    void getByIdShouldReturnMapperResult() throws Exception {
        Recorder handler = new Recorder();
        Object taskLog = newTaskLog(3, "third log");
        handler.selectByIdResult = taskLog;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Integer.class}, 3);

        assertSame(taskLog, result);
    }

    @Test
    void getByIdShouldReturnNullWhenTaskLogMissing() throws Exception {
        Recorder handler = new Recorder();
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Integer.class}, 100);

        assertNull(result);
    }

    @Test
    void getAllShouldReturnAllTaskLogs() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(1, "a"), newTaskLog(2, "b"));
        handler.selectAllResult = taskLogs;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAll", new Class<?>[0]);

        assertEquals(taskLogs, result);
    }

    @Test
    void getAllByOwnerIdShouldReturnOwnerTaskLogs() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(3, "owner-log"));
        handler.selectAllByOwnerIdResult = taskLogs;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAllByOwnerId", new Class<?>[]{Long.class}, 9L);

        assertEquals(taskLogs, result);
        assertEquals("selectAllByOwnerId", handler.lastMethodName);
        assertEquals(9L, handler.lastArgs[0]);
    }

    @Test
    void getAllByParticipantIdShouldReturnParticipantTaskLogs() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(4, "participant-log"));
        handler.selectAllByParticipantIdResult = taskLogs;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAllByParticipantId", new Class<?>[]{Long.class}, 2L);

        assertEquals(taskLogs, result);
        assertEquals("selectAllByParticipantId", handler.lastMethodName);
        assertEquals(2L, handler.lastArgs[0]);
    }

    @Test
    void getAllByOperatorIdShouldReturnOperatorTaskLogs() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(5, "operator-log"));
        handler.selectAllByOperatorIdResult = taskLogs;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAllByOperatorId", new Class<?>[]{Long.class}, 1L);

        assertEquals(taskLogs, result);
        assertEquals("selectAllByOperatorId", handler.lastMethodName);
        assertEquals(1L, handler.lastArgs[0]);
    }

    private Object newService(Object mapper) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.impl.TaskLogImpl");
        Class<?> mapperClass = Class.forName("com.example.end.mapper.TaskLogMapper");
        Constructor<?> constructor = serviceClass.getConstructor(mapperClass);
        return constructor.newInstance(mapper);
    }

    private Object newMapper(Recorder handler) throws Exception {
        Class<?> mapperClass = Class.forName("com.example.end.mapper.TaskLogMapper");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{mapperClass}, handler);
    }

    private Object newTaskLog(Integer id, String content) throws Exception {
        Class<?> taskLogClass = Class.forName("com.example.end.pojo.TaskLog");
        Object taskLog = taskLogClass.getConstructor().newInstance();
        invoke(taskLog, "setId", new Class<?>[]{Integer.class}, id);
        invoke(taskLog, "setOperatorId", new Class<?>[]{Long.class}, 1L);
        invoke(taskLog, "setTaskId", new Class<?>[]{Long.class}, 1L);
        invoke(taskLog, "setProgressPercent", new Class<?>[]{Integer.class}, 50);
        invoke(taskLog, "setContent", new Class<?>[]{String.class}, content);
        return taskLog;
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
        private List<Object> selectAllByOperatorIdResult = new ArrayList<>();

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
                case "selectAllByOperatorId" -> selectAllByOperatorIdResult;
                default -> null;
            };
        }
    }
}
