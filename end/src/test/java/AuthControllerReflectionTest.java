import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthControllerReflectionTest {

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() throws Exception {
        Recorder handler = new Recorder();
        handler.getByUsernameResult = newUser("alice", "123456");
        Object controller = newController(newAuthService(handler));
        Object request = newLoginRequest("alice", "123456");

        Object result = invoke(controller, "login", new Class<?>[]{Class.forName("com.example.end.auth.AuthLoginRequest")}, request);

        assertEquals("getByUsername", handler.lastMethodName);
        assertEquals(200, invokeGetter(result, "getCode"));
        assertEquals("success", invokeGetter(result, "getMessage"));
        assertNotNull(invokeGetter(invokeGetter(result, "getData"), "getToken"));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        Recorder handler = new Recorder();
        handler.getByUsernameResult = null;
        Object controller = newController(newAuthService(handler));
        Object request = newLoginRequest("alice", "wrong");

        Object result = invoke(controller, "login", new Class<?>[]{Class.forName("com.example.end.auth.AuthLoginRequest")}, request);

        assertEquals(401, invokeGetter(result, "getCode"));
        assertEquals("username or password is incorrect", invokeGetter(result, "getMessage"));
        assertNull(invokeGetter(result, "getData"));
    }

    private Object newController(Object service) throws Exception {
        Class<?> controllerClass = Class.forName("com.example.end.auth.AuthController");
        Class<?> serviceClass = Class.forName("com.example.end.auth.AuthService");
        Constructor<?> constructor = controllerClass.getConstructor(serviceClass);
        return constructor.newInstance(service);
    }

    private Object newAuthService(Recorder handler) throws Exception {
        Class<?> authServiceClass = Class.forName("com.example.end.auth.AuthService");
        Class<?> sysUserServiceClass = Class.forName("com.example.end.service.SysUserService");
        Class<?> jwtPropertiesClass = Class.forName("com.example.end.auth.JwtProperties");
        Object sysUserService = Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{sysUserServiceClass}, handler);
        Object jwtProperties = jwtPropertiesClass.getConstructor().newInstance();
        invoke(jwtProperties, "setSecret", new Class<?>[]{String.class}, "test-secret-key");
        invoke(jwtProperties, "setExpireSeconds", new Class<?>[]{long.class}, 3600L);
        Constructor<?> constructor = authServiceClass.getConstructor(sysUserServiceClass, jwtPropertiesClass, ObjectMapper.class);
        return constructor.newInstance(sysUserService, jwtProperties, new ObjectMapper());
    }

    private Object newLoginRequest(String username, String password) throws Exception {
        Class<?> requestClass = Class.forName("com.example.end.auth.AuthLoginRequest");
        Object request = requestClass.getConstructor().newInstance();
        invoke(request, "setUsername", new Class<?>[]{String.class}, username);
        invoke(request, "setPassword", new Class<?>[]{String.class}, password);
        return request;
    }

    private Object newUser(String username, String password) throws Exception {
        Class<?> userClass = Class.forName("com.example.end.pojo.SysUser");
        Object user = userClass.getConstructor().newInstance();
        invoke(user, "setId", new Class<?>[]{Long.class}, 1L);
        invoke(user, "setUsername", new Class<?>[]{String.class}, username);
        invoke(user, "setPassword", new Class<?>[]{String.class}, password);
        invoke(user, "setRealName", new Class<?>[]{String.class}, "Alice");
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
        private Object getByUsernameResult;

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            lastMethodName = method.getName();
            return switch (method.getName()) {
                case "getByUsername" -> getByUsernameResult;
                case "getAll" -> List.of();
                default -> null;
            };
        }
    }
}
