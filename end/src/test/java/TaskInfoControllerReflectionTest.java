import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskInfoControllerReflectionTest {

    @Test
    void addShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object task = newTask(1L, "task-a");
        handler.addResult = task;
        Object controller = newController(newService(handler), newAccessService(true, 9L));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.TaskInfo")}, task);

        assertEquals("add", handler.lastMethodName);
        assertSame(task, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(task, invokeGetter(result, "getData"));
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
        Object task = newTask(2L, "task-b");
        handler.getByIdResult = task;
        Object controller = newController(newService(handler), newAccessService(true, 9L));

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.TaskInfo")}, task);

        assertEquals(404, invokeGetter(result, "getCode"));
        assertEquals("task not found", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
    }

    @Test
    void getByIdShouldReturnWrappedTask() throws Exception {
        Recorder handler = new Recorder();
        Object task = newTask(3L, "task-c");
        handler.getByIdResult = task;
        Object controller = newController(newService(handler), newAccessService(false, 2L));

        Object result = invoke(controller, "getById", new Class<?>[]{Long.class}, 3L);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(task, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnWrappedList() throws Exception {
        Recorder handler = new Recorder();
        List<Object> tasks = List.of(newTask(1L, "a"), newTask(2L, "b"));
        handler.getAllResult = tasks;
        Object controller = newController(newService(handler), newAccessService(false, 2L));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(tasks, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnOwnerTasksForProjectOwner() throws Exception {
        Recorder handler = new Recorder();
        List<Object> tasks = List.of(newTask(3L, "owner-task"));
        handler.getAllByOwnerIdResult = tasks;
        Object controller = newController(newService(handler), newAccessService(false, 9L, 1));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAllByOwnerId", handler.lastMethodName);
        assertEquals(9L, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(tasks, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnParticipantTasksForEmployee() throws Exception {
        Recorder handler = new Recorder();
        List<Object> tasks = List.of(newTask(4L, "employee-task"));
        handler.getAllByParticipantIdResult = tasks;
        Object controller = newController(newService(handler), newAccessService(false, 2L, 2));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAllByParticipantId", handler.lastMethodName);
        assertEquals(2L, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(tasks, invokeGetter(result, "getData"));
    }

    private Object newController(Object service, Object accessService) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.controller.TaskInfoController");
        Class<?> serviceClass = Class.forName("com.example.end.service.TaskInfoService");
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass, accessServiceClass);
        return constructor.newInstance(service, accessService);
    }

    private Object newService(Recorder handler) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.TaskInfoService");
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
