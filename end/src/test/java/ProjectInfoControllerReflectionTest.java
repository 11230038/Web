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

class ProjectInfoControllerReflectionTest {

    @Test
    void addShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object project = newProject(1L, "demo");
        handler.addResult = project;
        Object controller = newController(newService(handler), newAccessService(true, 9L, null));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.ProjectInfo")}, project);

        assertEquals("add", handler.lastMethodName);
        assertSame(project, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(project, invokeGetter(result, "getData"));
    }

    @Test
    void deleteByIdShouldReturnSuccessWhenDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = true;
        Object controller = newController(newService(handler), newAccessService(true, 9L, null));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Long.class}, 1L);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertTrue((Boolean) invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = false;
        Object controller = newController(newService(handler), newAccessService(true, 9L, null));
        Object project = newProject(2L, "build");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.ProjectInfo")}, project);

        assertEquals(404, invokeGetter(result, "getCode"));
        assertEquals("project not found", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
    }

    @Test
    void getByIdShouldReturnWrappedProject() throws Exception {
        Recorder handler = new Recorder();
        Object project = newProject(3L, "ship");
        handler.getByIdResult = project;
        Object controller = newController(newService(handler), newAccessService(false, 9L, null));

        Object result = invoke(controller, "getById", new Class<?>[]{Long.class}, 3L);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertSame(project, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldReturnWrappedList() throws Exception {
        Recorder handler = new Recorder();
        List<Object> projects = List.of(newProject(1L, "a"), newProject(2L, "b"));
        handler.getAllResult = projects;
        Object controller = newController(newService(handler), newAccessService(false, 9L, null));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals(projects, invokeGetter(result, "getData"));
    }

    private Object newController(Object service, Object accessService) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.controller.ProjectInfoController");
        Class<?> serviceClass = Class.forName("com.example.end.service.ProjectInfoService");
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass, accessServiceClass);
        return constructor.newInstance(service, accessService);
    }

    private Object newService(Recorder handler) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.ProjectInfoService");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{serviceClass}, handler);
    }

    private Object newAccessService(boolean manager, Long currentUserId, Object currentUser) throws Exception {
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{accessServiceClass}, (proxy, method, args) -> switch (method.getName()) {
            case "isManager" -> manager;
            case "currentUserId" -> currentUserId;
            case "currentUser" -> currentUser;
            case "isCurrentUser" -> args != null && args.length > 0 && currentUserId.equals(args[0]);
            default -> null;
        });
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
                default -> null;
            };
        }
    }
}
