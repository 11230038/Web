import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SysUserControllerReflectionTest {

    @Test
    void addShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object user = newUser(1L, "alice");
        handler.addResult = user;
        Object controller = newController(newService(handler));

        Object result = invoke(controller, "add", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals("add", handler.lastMethodName);
        assertSame(user, handler.lastArgs[0]);
        assertSame(user, result);
    }

    @Test
    void deleteByIdShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = true;
        Object controller = newController(newService(handler));

        Object result = invoke(controller, "deleteById", new Class<?>[]{Long.class}, 1L);

        assertEquals("deleteById", handler.lastMethodName);
        assertTrue((Boolean) result);
    }

    @Test
    void updateByIdShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        handler.updateResult = false;
        Object controller = newController(newService(handler));
        Object user = newUser(2L, "bob");

        Object result = invoke(controller, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals("updateById", handler.lastMethodName);
        assertFalse((Boolean) result);
    }

    @Test
    void getByIdShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        Object user = newUser(3L, "carol");
        handler.getByIdResult = user;
        Object controller = newController(newService(handler));

        Object result = invoke(controller, "getById", new Class<?>[]{Long.class}, 3L);

        assertEquals("getById", handler.lastMethodName);
        assertSame(user, result);
    }

    @Test
    void getAllShouldDelegateToService() throws Exception {
        Recorder handler = new Recorder();
        List<Object> users = List.of(newUser(1L, "alice"), newUser(2L, "bob"));
        handler.getAllResult = users;
        Object controller = newController(newService(handler));

        Object result = invoke(controller, "getAll", new Class<?>[0]);

        assertEquals("getAll", handler.lastMethodName);
        assertEquals(users, result);
    }

    private Object newController(Object service) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.controller.SysUserController");
        Class<?> serviceClass = Class.forName("com.example.end.service.SysUserService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass);
        return constructor.newInstance(service);
    }

    private Object newService(Recorder handler) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.SysUserService");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{serviceClass}, handler);
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
