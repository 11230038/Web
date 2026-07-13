# 项目环境总览

这份文档专门记录当前项目 `front` 和 `end` 所使用的全部环境、默认配置与启动方式。

## 目录对应

- 前端：`front/`
- 后端：`end/`
- 根目录：`Web/`

## 整体技术栈

- 前端：Vue 3、Vite
- 后端：Spring Boot 4、MyBatis
- 数据库：MySQL 8.x
- 构建工具：npm、Maven
- Java 版本：JDK 17

## 前端环境

### 基础环境

- Node.js：`^20.19.0 || >=22.12.0`
- 包管理：`npm`
- 模块类型：`ES Module`

### 前端依赖

来自 [front/package.json]：

- `vue`：`^3.5.27`
- `vite`：`^7.3.1`
- `@vitejs/plugin-vue`：`^6.0.3`
- `vite-plugin-vue-devtools`：`^8.0.5`

### 前端脚本

在 `front/` 目录执行：

```bash
npm install
npm run dev
npm run build
npm run preview
```

### 前端代理配置

来自 [front/vite.config.js](/C:/Users/鄭昊/Desktop/Web/front/vite.config.js)：

- Vite 开发服务器通过代理把以下请求转发到 `http://127.0.0.1:8080`
- 代理路径：
  - `/auth`
  - `/projectInfos`
  - `/taskInfos`
  - `/taskLogs`
  - `/taskSummaries`
  - `/sysUsers`
  - `/operateLogs`
  - `/api`

说明：
- `vite.config.js` 中没有显式指定前端端口，因此开发端口使用 Vite 默认值。
- 前端目前未发现 `VITE_` 开头的环境变量配置。

## 后端环境

### 基础环境

- JDK：`17`
- Maven：建议 `3.9+`
- 打包产物：`end/target/end-0.0.1-SNAPSHOT.jar`

### 后端核心依赖

来自 [end/pom.xml](/C:/Users/鄭昊/Desktop/Web/end/pom.xml)：

- `spring-boot-starter-parent`：`4.0.2`
- `spring-boot-starter-webmvc`
- `spring-aop`：`7.0.2`
- `aspectjweaver`：`1.9.25.1`
- `mybatis-spring-boot-starter`：`4.0.1`
- `mysql-connector-j`
- `lombok`：`1.18.42`

### 后端脚本

在 `end/` 目录执行：

```bash
mvn spring-boot:run
mvn test
mvn package
java -jar target/end-0.0.1-SNAPSHOT.jar
```

## 数据库环境

### 基础要求

- 数据库：MySQL `8.x`
- 默认库名：`mini_semester`
- 字符集建议：`utf8mb4`

### 建库语句

```sql
CREATE DATABASE mini_semester CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 初始化脚本

后端启动时会自动执行：

- [end/src/main/resources/schema.sql]
- [end/src/main/resources/data.sql]

说明：
- `schema.sql` 负责建表
- `data.sql` 负责初始化演示数据

## 后端配置项

来自 [end/src/main/resources/application.properties]：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mini_semester?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=230038
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.config.import=optional:classpath:application-ai.properties

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

mybatis.configuration.map-underscore-to-camel-case=true
mybatis.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl

jwt.secret=mini-semester-jwt-secret-change-me
jwt.expire-seconds=86400
```

### 已确认的默认配置

- 后端服务端口：未显式配置，默认使用 Spring Boot 默认端口 `8080`
- 数据库地址：`localhost:3306`
- 数据库名：`mini_semester`
- 数据库账号：`root`
- 数据库密码：`230038`（需要改成你的实际密码）
- JWT 有效期：`86400` 秒

## 可选 AI 环境

后端会尝试加载可选文件：

- `end/src/main/resources/application-ai.properties`

如果未提供该文件：

- 后端仍可正常启动
- 只是 AI 相关能力不可用或走兜底逻辑

建议配置项：

```properties
ai.base-url=你的模型服务地址
ai.api-key=你的密钥
ai.model=你的模型名称
```

## 启动顺序建议

1. 启动 MySQL，并创建 `mini_semester`
2. 在 `end/` 启动后端
3. 在 `front/` 启动前端

对应命令：

```bash
cd end
mvn spring-boot:run
```

```bash
cd front
npm install
npm run dev
```

## 当前环境文件结论

目前仓库里实际已确认到的环境信息有：

- 前端使用 Node.js + npm
- 后端使用 JDK 17 + Maven + Spring Boot 4
- 数据库使用 MySQL 8.x
- 前端开发代理指向后端 `127.0.0.1:8080`
- 后端默认连接 `localhost:3306/mini_semester`
- AI 配置为可选独立文件

## 维护建议

- 后续如果增加 `.env`、`application-*.properties` 或 Docker 配置，可以继续补充到这份文档
- 当前数据库密码和 JWT 密钥仍是明文默认值，部署前建议改为环境变量或外部配置
