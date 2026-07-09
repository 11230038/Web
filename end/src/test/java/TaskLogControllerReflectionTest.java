import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskLogControllerReflectionTest {

    @Test
    void addShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object taskLog = newTaskLog(1, "first log");
        handler.addResult = taskLog;
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.TaskLog")}, taskLog);

        assertEquals("add", handler.lastMethodName);
        assertSame(taskLog, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(taskLog, invokeGetter(result, "getData"));
    }

    @Test
    void deleteByIdShouldReturnSuccessWhenDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = true;
        handler.getByIdResult = newTaskLog(1, "first log");
        Object controller = newController(newService(handler), newAccessService(true, 9L));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Integer.class}, 1);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertTrue((Boolean) invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = false;
        handler.getByIdResult = newTaskLog(2, "second log");
        Object controller = newController(newService(handler), newAccessService(true, 9L));
        Object taskLog = newTaskLog(2, "second log");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskLog")}, taskLog);

        assertEquals(404, invokeGetter(result, "getCode"));
        assertEquals("task log not found", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
    }

    @Test
    void getByIdShouldReturnWrappedTaskLog() throws Exception {
        Recorder handler = new Recorder();
        Object taskLog = newTaskLog(3, "third log");
        handler.getByIdResult = taskLog;
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "getById", new Class<?>[]{Integer.class}, 3);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(taskLog, invokeGetter(result, "getData"));
    }

    @Test
    void deleteByIdShouldAllowOperatorToDeleteOwnLog() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = true;
        handler.getByIdResult = newTaskLog(5, "mine");
        Object controller = newController(newService(handler), newAccessService(false, 1L));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Integer.class}, 5);

        assertEquals("deleteById", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertTrue((Boolean) invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldAllowOperatorToUpdateOwnLog() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = true;
        handler.getByIdResult = newTaskLog(6, "before");
        Object controller = newController(newService(handler), newAccessService(false, 1L));
        Object taskLog = newTaskLog(6, "after");
        invoke(taskLog, "setOperatorId", new Class<?>[]{Long.class}, 99L);

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskLog")}, taskLog);

        assertEquals("updateById", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        Object updatedLog = handler.lastArgs[0];
        assertEquals(1L, invokeGetter(updatedLog, "getOperatorId"));
    }

    @Test
    void deleteByIdShouldReturnForbiddenWhenNonOwnerDeletesLog() throws Exception {
        Recorder handler = new Recorder();
        handler.getByIdResult = newTaskLog(7, "other");
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Integer.class}, 7);

        assertEquals(403, invokeGetter(result, "getCode"));
        assertEquals("forbidden", invokeGetter(result, "getMessage"));
    }

    @Test
    void updateByIdShouldReturnForbiddenWhenNonOwnerUpdatesLog() throws Exception {
        Recorder handler = new Recorder();
        handler.getByIdResult = newTaskLog(8, "other");
        Object controller = newController(newService(handler), newAccessService(false, 9L));
        Object taskLog = newTaskLog(8, "try update");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskLog")}, taskLog);

        assertEquals(403, invokeGetter(result, "getCode"));
        assertEquals("forbidden", invokeGetter(result, "getMessage"));
    }

    @Test
    void getAllShouldReturnWrappedList() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(1, "a"), newTaskLog(2, "b"));
        handler.getAllResult = taskLogs;
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(taskLogs, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnOwnerTaskLogsForProjectOwner() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(3, "owner-log"));
        handler.getAllByOwnerIdResult = taskLogs;
        Object controller = newController(newService(handler), newAccessService(false, 9L, 1));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAllByOwnerId", handler.lastMethodName);
        assertEquals(9L, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(taskLogs, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnParticipantTaskLogsForEmployee() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskLogs = List.of(newTaskLog(4, "employee-log"));
        handler.getAllByOperatorIdResult = taskLogs;
        Object controller = newController(newService(handler), newAccessService(false, 2L, 2));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAllByOperatorId", handler.lastMethodName);
        assertEquals(2L, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(taskLogs, invokeGetter(result, "getData"));
    }

    private Object newController(Object service, Object accessService) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.controller.TaskLogController");
        Class<?> serviceClass = Class.forName("com.example.end.service.TaskLogService");
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass, accessServiceClass);
        return constructor.newInstance(service, accessService);
    }

    private Object newService(Recorder handler) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.TaskLogService");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{serviceClass}, handler);
    }

    private Object newAccessService(boolean manager, Long currentUserId) throws Exception {
        return newAccessService(manager, currentUserId, null);
    }

    private Object newAccessService(boolean manager, Long currentUserId, Integer role) throws Exception {
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Object currentUser = role == null ? null : newUser(currentUserId, role);
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{accessServiceClass}, (proxy, method, args) -> switch (method.getName()) {
            case "isManager" -> manager;
            case "currentUserId" -> currentUserId;
            case "currentUser" -> currentUser;
            case "isCurrentUser" -> args != null && args.length > 0 && currentUserId.equals(args[0]);
            default -> null;
        });
    }

    private Object newUser(Long id, Integer role) throws Exception {
        Class<?> userClass = Class.forName("com.example.end.pojo.SysUser");
        Object user = userClass.getConstructor().newInstance();
        invoke(user, "setId", new Class<?>[]{Long.class}, id);
        invoke(user, "setRole", new Class<?>[]{Integer.class}, role);
        return user;
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

    private Object invokeGetter(Object target, String methodName) throws Exception {
        Method method = target.getClass().getMethod(methodName);
        return method.invoke(target);
    }

    private static class Recorder implements InvocationHandler {
        private String lastMethodName;
        private Object[] lastArgs = new Object[0];
        private Object addResult;
        private boolean deleteResult;
        private boolean updateResult;
        private Object getByIdResult;
        private List<Object> getAllResult = List.of();
        private List<Object> getAllByOwnerIdResult = List.of();
        private List<Object> getAllByParticipantIdResult = List.of();
        private List<Object> getAllByOperatorIdResult = List.of();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            lastMethodName = method.getName();
            lastArgs = args == null ? new Object[0] : args;
            return switch (method.getName()) {
                case "add" -> addResult;
                case "deleteById" -> deleteResult;
                case "updateById" -> updateResult;
                case "getById" -> getByIdResult;
                case "getAll" -> getAllResult;
                case "getAllByOwnerId" -> getAllByOwnerIdResult;
                case "getAllByParticipantId" -> getAllByParticipantIdResult;
                case "getAllByOperatorId" -> getAllByOperatorIdResult;
                default -> null;
            };
        }
    }
}
