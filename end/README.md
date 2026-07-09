# Backend

后端基于 Spring Boot + MyBatis + MySQL，提供登录鉴权、成员、项目、任务、进度记录、总结中心，以及可选的 AI 拆解接口。

## 常用命令

```bash
mvn spring-boot:run
mvn test
mvn package
```

## 关键目录

- `src/main/java/com/example/end/controller/`：接口层
- `src/main/java/com/example/end/service/`：业务层
- `src/main/java/com/example/end/mapper/`：数据访问层
- `src/main/java/com/example/end/auth/`：JWT 与登录鉴权
- `src/main/java/com/example/end/agent/`：AI 能力相关逻辑
- `src/main/resources/`：配置、建表脚本、初始化数据

更完整的项目说明见根目录 [README.md](/C:/Users/鄭昊/Desktop/Web/README.md)。
