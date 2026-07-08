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

class SysUserImplReflectionTest {

    @Test
    void addShouldReturnInsertedUser() throws Exception {
        Class<?> mapperClass = Class.forName("com.example.end.mapper.SysUserMapper");
        Recorder handler = new Recorder();
        Object mapper = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{mapperClass}, handler);
        Object service = newService(mapper);
        Object user = newUser(1L, "alice");

        Object result = invoke(service, "add", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertSame(user, result);
        assertEquals("insert", handler.lastMethodName);
        assertSame(user, handler.lastArgs[0]);
    }

    @Test
    void deleteByIdShouldReturnTrueWhenRowDeleted() throws Exception {
        Recorder handler = new Recorder();
        handler.deleteResult = 1;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "deleteById", new Class<?>[]{Long.class}, 1L);

        assertEquals("deleteById", handler.lastMethodName);
        assertEquals(1L, handler.lastArgs[0]);
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
        Object user = newUser(2L, "bob");

        Object result = invoke(service, "updateById", new Class<?>[]{Class.forName("com.example.end.pojo.SysUser")}, user);

        assertEquals("updateById", handler.lastMethodName);
        assertSame(user, handler.lastArgs[0]);
        assertTrue((Boolean) result);
    }

    @Test
    void getByIdShouldReturnMapperResult() throws Exception {
        Recorder handler = new Recorder();
        Object user = newUser(3L, "carol");
        handler.selectByIdResult = user;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 3L);

        assertSame(user, result);
    }

    @Test
    void getByIdShouldReturnNullWhenUserMissing() throws Exception {
        Recorder handler = new Recorder();
        handler.selectByIdResult = null;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getById", new Class<?>[]{Long.class}, 100L);

        assertNull(result);
    }

    @Test
    void getAllShouldReturnAllUsers() throws Exception {
        Recorder handler = new Recorder();
        List<Object> users = List.of(newUser(1L, "alice"), newUser(2L, "bob"));
        handler.selectAllResult = users;
        Object service = newService(newMapper(handler));

        Object result = invoke(service, "getAll", new Class<?>[0]);

        assertEquals(users, result);
    }

    private Object newService(Object mapper) throws Exception {
        Class<?> serviceClass = Class.forName("com.example.end.service.impl.SysUserImpl");
        Class<?> mapperClass = Class.forName("com.example.end.mapper.SysUserMapper");
        Constructor<?> constructor = serviceClass.getConstructor(mapperClass);
        return constructor.newInstance(mapper);
    }

    private Object newMapper(Recorder handler) throws Exception {
        Class<?> mapperClass = Class.forName("com.example.end.mapper.SysUserMapper");
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{mapperClass}, handler);
    }

    private Object newUser(Long id, String username) throws Exception {
        Class<?> userClass = Class.forName("com.example.end.pojo.SysUser");
        Object user = userClass.getConstructor().newInstance();
        invoke(user, "setId", new Class<?>[]{Long.class}, id);
        invoke(user, "setUsername", new Class<?>[]{String.class}, username);
        invoke(user, "setPassword", new Class<?>[]{String.class}, "123456");
        invoke(user, "setRealName", new Class<?>[]{String.class}, "Test User");
        invoke(user, "setRole", new Class<?>[]{Integer.class}, 1);
        invoke(user, "setEmail", new Class<?>[]{String.class}, username + "@example.com");
        invoke(user, "setPhone", new Class<?>[]{Integer.class}, 123456789);
        return user;
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
