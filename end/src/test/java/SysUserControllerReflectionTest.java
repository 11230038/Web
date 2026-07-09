import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysUserControllerReflectionTest {

    @Test
    void addShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object user = newUser(1L, "alice");
        handler.addResult = user;
        invoke(user, "setRole", new Class<?>[]{Integer.class}, 0);
        Object controller = newController(newService(handler), newAccessService(true, user));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals("add", handler.lastMethodName);
        assertSame(user, handler.lastArgs[0]);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals("success", invokeGetter(result, "getMessage"));
        assertSame(user, invokeGetter(result, "getData"));
    }

    @Test
    void addShouldReturnForbiddenWhenCurrentUserIsNotAdmin() throws Exception {
        Recorder handler = new Recorder();
        Object user = newUser(2L, "bob");
        invoke(user, "setRole", new Class<?>[]{Integer.class}, 1);
        Object controller = newController(newService(handler), newAccessService(true, user));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals(403, invokeGetter(result, "getCode"));
        assertEquals("forbidden", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
        assertFalse("add".equals(handler.lastMethodName));
    }

    @Test
    void deleteByIdShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = true;
        Object controller = newController(newService(handler), newAccessService(true, newUser(1L, "alice")));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Long.class}, 1L);

        assertEquals("deleteById", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals("success", invokeGetter(result, "getMessage"));
        assertTrue((Boolean) invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldReturnNotFoundWhenServiceFails() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = false;
        Object controller = newController(newService(handler), newAccessService(true, newUser(1L, "alice")));
        Object user = newUser(2L, "bob");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals("updateById", handler.lastMethodName);
        assertEquals(404, invokeGetter(result, "getCode"));
        assertEquals("user not found", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldAllowCurrentUserToUpdateOwnProfile() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = true;
        Object currentUser = newUser(2L, "bob");
        handler.getByIdResult = currentUser;
        Object controller = newController(newService(handler), newAccessService(false, currentUser));
        Object user = newUser(2L, "bob");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals("updateById", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals("success", invokeGetter(result, "getMessage"));
        assertTrue((Boolean) invokeGetter(result, "getData"));
    }

    @Test
    void updateByIdShouldRejectOtherUserWhenNotManager() throws Exception {
        Recorder handler = new Recorder();
        Object controller = newController(newService(handler), newAccessService(false, newUser(1L, "alice")));
        Object user = newUser(2L, "bob");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals(403, invokeGetter(result, "getCode"));
        assertEquals("forbidden", invokeGetter(result, "getMessage"));
        assertFalse("updateById".equals(handler.lastMethodName));
    }

    @Test
    void getByIdShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object user = newUser(3L, "carol");
        handler.getByIdResult = user;
        Object controller = newController(newService(handler), newAccessService(false, user));

        Object result = invoke(controller, "getById", new Class<?>[]{Long.class}, 3L);

        assertEquals("getById", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals("success", invokeGetter(result, "getMessage"));
        assertSame(user, invokeGetter(result, "getData"));
    }

    @Test
    void getAllShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        List<Object> users = List.of(newUser(1L, "alice"), newUser(2L, "bob"));
        handler.getAllResult = users;
        Object controller = newController(newService(handler), newAccessService(false, newUser(1L, "alice")));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAll", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals("success", invokeGetter(result, "getMessage"));
        assertEquals(users, invokeGetter(result, "getData"));
    }

    private Object newController(Object service, Object accessService) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.controller.SysUserController");
        Class<?> serviceClass = Class.forName("com.example.end.service.SysUserService");
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass, accessServiceClass);
        return constructor.newInstance(service, accessService);
    }

    private Object newService(Recorder handler) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.SysUserService");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{serviceClass}, handler);
    }

    private Object newAccessService(boolean manager, Object currentUser) throws Exception {
        Class<?> accessServiceClass = Class.forName("com.example.end.auth.AccessService");
        Long currentUserId = currentUser == null ? null : (Long) invokeGetter(currentUser, "getId");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{accessServiceClass}, (proxy, method, args) -> switch (method.getName()) {
            case "isManager" -> manager;
            case "currentUserId" -> currentUserId;
            case "currentUser" -> currentUser;
            case "isCurrentUser" -> args != null && args.length > 0 && currentUserId != null && currentUserId.equals(args[0]);
            default -> null;
        });
    }

    private Object newUser(Long id, String username) throws Exception {
        Class<?> userClass = Class.forName("com.example.end.pojo.SysUser");
        Object user = userClass.getConstructor().newInstance();
        invoke(user, "setId", new Class<?>[]{Long.class}, id);
        invoke(user, "setUsername", new Class<?>[]{String.class}, username);
        invoke(user, "setPassword", new Class<?>[]{String.class}, "123456");
        invoke(user, "setRealName", new Class<?>[]{String.class}, "Test User");
        invoke(user, "setRole", new Class<?>[]{Integer.class}, 1);
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
