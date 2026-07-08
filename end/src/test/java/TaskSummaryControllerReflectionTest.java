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

class TaskSummaryControllerReflectionTest {

    @Test
    void addShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object taskSummary = newTaskSummary(1L, 0);
        handler.addResult = taskSummary;
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.TaskSummary")}, taskSummary);

        assertEquals("add", handler.lastMethodName);
        assertSame(taskSummary, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(taskSummary, invokeGetter(result, "getData"));
    }

    @Test
    void deleteByIdShouldReturnSuccessWhenDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = true;
        Object controller = newController(newService(handler), newAccessService(true, 9L));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Long.class}, 1L);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertTrue((Boolean) invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = false;
        Object controller = newController(newService(handler), newAccessService(true, 9L));
        Object taskSummary = newTaskSummary(2L, 1);

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskSummary")}, taskSummary);

        assertEquals(404, invokeGetter(result, "getCode"));
        assertEquals("task summary not found", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
    }

    @Test
    void getByIdShouldReturnWrappedTaskSummary() throws Exception {
        Recorder handler = new Recorder();
        Object taskSummary = newTaskSummary(3L, 1);
        handler.getByIdResult = taskSummary;
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "getById", new Class<?>[]{Long.class}, 3L);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(taskSummary, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnWrappedList() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskSummaries = List.of(newTaskSummary(1L, 0), newTaskSummary(2L, 1));
        handler.getAllResult = taskSummaries;
        Object controller = newController(newService(handler), newAccessService(false, 9L));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(taskSummaries, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnOwnerTaskSummariesForProjectOwner() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskSummaries = List.of(newTaskSummary(3L, 0));
        handler.getAllByOwnerIdResult = taskSummaries;
        Object controller = newController(newService(handler), newAccessService(false, 9L, 1));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAllByOwnerId", handler.lastMethodName);
        assertEquals(9L, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(taskSummaries, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnParticipantTaskSummariesForEmployee() throws Exception {
        Recorder handler = new Recorder();
        List<Object> taskSummaries = List.of(newTaskSummary(4L, 1));
        handler.getAllByParticipantIdResult = taskSummaries;
        Object controller = newController(newService(handler), newAccessService(false, 2L, 2));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAllByParticipantId", handler.lastMethodName);
        assertEquals(2L, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(taskSummaries, invokeGetter(result, "getData"));
    }

    private Object newController(Object service, Object accessService) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.controller.TaskSummaryController");
        Class<?> serviceClass = Class.forName("com.example.end.service.TaskSummaryService");
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass, accessServiceClass);
        return constructor.newInstance(service, accessService);
    }

    private Object newService(Recorder handler) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.TaskSummaryService");
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

    private Object newTaskSummary(Long id, Integer summaryType) throws Exception {
        Class<?> taskSummaryClass = Class.forName("com.example.end.pojo.TaskSummary");
        Object taskSummary = taskSummaryClass.getConstructor().newInstance();
        invoke(taskSummary, "setId", new Class<?>[]{Long.class}, id);
        invoke(taskSummary, "setCreatorId", new Class<?>[]{Long.class}, 1L);
        invoke(taskSummary, "setProjectId", new Class<?>[]{Long.class}, 1L);
        invoke(taskSummary, "setTaskId", new Class<?>[]{Integer.class}, 1);
        invoke(taskSummary, "setSummaryType", new Class<?>[]{Integer.class}, summaryType);
        invoke(taskSummary, "setContent", new Class<?>[]{String.class}, "content");
        return taskSummary;
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
                default -> null;
            };
        }
    }
}
