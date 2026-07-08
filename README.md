# 项目协作与任务管理系统

一个基于 Vue 3 + Vite + Spring Boot + MyBatis + MySQL 的轻量级项目协作系统，支持项目管理、任务管理、进度跟踪、总结中心、成员管理，以及 AI 辅助任务拆解。

## 项目结构

```text
Web/
├─ front/    前端，Vue 3 + Vite
└─ end/      后端，Spring Boot + MyBatis
```

## 功能概览

- 用户登录与基于 Token 的接口鉴权
- 项目新增、编辑、删除、查看
- 任务新增、编辑、删除、状态更新
- 进度跟踪与任务日志管理
- 总结中心
- 成员管理与密码修改
- AI 项目拆解，并可一键导入为任务

## 当前权限规则

- 管理员可以查看全部项目、任务、日志、总结和成员。
- 普通成员可以新增项目；新增后会自动升级为项目负责人。
- 项目负责人只能看到自己负责的项目。
- 任务、进度跟踪、总结中心只显示与当前用户相关的项目或任务。
- 新增成员只有管理员有权限。
- AI 拆解时，Agent 当前只会拿到：
  - 当前项目负责人
  - 所有普通成员

## 技术栈

- 前端：Vue 3、Vite
- 后端：Spring Boot 4、MyBatis
- 数据库：MySQL
- 鉴权：自定义 JWT Token
- AI：后端通过可选 `application-ai.properties` 接入大模型

## 环境要求

- Node.js `^20.19.0 || >=22.12.0`
- JDK 17
- Maven 3.9+
- MySQL 8.x

## 快速开始

### 1. 准备数据库

先创建数据库：

```sql
CREATE DATABASE mini_semester CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

后端启动时会自动执行：

- `end/src/main/resources/schema.sql`
- `end/src/main/resources/data.sql`

也就是说，表结构和演示数据会自动初始化。

### 2. 检查后端配置

当前后端配置文件是 [application.properties](C:/Users/鄭昊/Desktop/Web/end/src/main/resources/application.properties)。

默认关键配置如下：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mini_semester?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=230038

jwt.secret=mini-semester-jwt-secret-change-me
jwt.expire-seconds=86400
```

如果你的本地 MySQL 用户名、密码或库名不同，请先修改这里。

### 3. 可选配置 AI

后端会按下面这行配置尝试加载 AI 配置：

```properties
spring.config.import=optional:classpath:application-ai.properties
```

这意味着没有 AI 配置也能启动，只是 AI 拆解功能不可用或回退为兜底结果。

你可以在 `end/src/main/resources/` 下新增 `application-ai.properties`：

```properties
ai.base-url=你的模型服务地址
ai.api-key=你的密钥
ai.model=你的模型名
```

## 启动方式

### 启动后端

在 `end/` 目录执行：

```bash
mvn spring-boot:run
```

或先打包再运行：

```bash
mvn package
java -jar target/end-0.0.1-SNAPSHOT.jar
```

### 启动前端

在 `front/` 目录执行：

```bash
npm install
npm run dev
```

前端默认使用 Vite 开发服务器。

### 生产打包

前端：

```bash
cd front
npm run build
```

产物目录：

- `front/dist`

后端：

```bash
cd end
mvn package
```

产物目录：

- `end/target/end-0.0.1-SNAPSHOT.jar`

## 默认演示账号

初始化数据来自 [data.sql](C:/Users/鄭昊/Desktop/Web/end/src/main/resources/data.sql)。

默认账号如下，密码均为 `123456`：

- 管理员：`alice`
- 项目负责人：`bob`
- 普通成员：`carol`

## 主要接口

- 登录：`POST /auth/login`
- 成员：`/sysUsers`
- 项目：`/projectInfos`
- 任务：`/taskInfos`
- 进度日志：`/taskLogs`
- 总结：`/taskSummaries`
- AI：`/api/ai/task-suggestion`、`/api/ai/project-breakdown`

说明：

- 除 `/auth/login` 外，其余接口默认都需要在请求头中携带 `token`。
- 前端当前也是通过请求头 `token` 传递登录态。

## AI 拆解说明

- 新建项目时可以选择是否进行 AI 拆解。
- 只有新增项目需要 AI 拆解，编辑项目不进入该流程。
- AI 拆解和“新建项目”已经合并为同一个弹窗。
- AI 返回的拆解任务可以勾选后导入当前项目。
- 如果 AI 给出的负责人无效，前端会自动兜底到当前用户或可用成员。

## 前端说明

前端已经从早期单文件写法中拆分为模块化结构，核心逻辑集中在：

- [useWorkspaceApp.js](C:/Users/鄭昊/Desktop/Web/front/src/composables/useWorkspaceApp.js)
- `front/src/components/workspace/`

当前页面主要包括：

- 总览
- 项目管理
- 任务管理
- 进度跟踪
- 总结中心
- 成员列表
- 个人信息

## 后端说明

后端按控制器、服务、Mapper 分层，核心目录包括：

- `end/src/main/java/com/example/end/controller`
- `end/src/main/java/com/example/end/service`
- `end/src/main/java/com/example/end/mapper`
- `end/src/main/java/com/example/end/agent`
- `end/src/main/java/com/example/end/auth`

其中：

- `auth` 目录负责登录、JWT 解析、权限上下文
- `agent` 目录负责 AI 任务建议和项目拆解

## 测试

后端可使用 Maven 运行测试：

```bash
cd end
mvn test
```

或直接打包，打包时会一并执行测试：

```bash
mvn package
```

## 已知注意事项

- `application.properties` 里目前写的是本地数据库账号密码，更适合开发环境，部署前建议改为环境变量或外部配置。
- 项目中部分历史中文文本存在编码残留，不影响构建与核心功能，但后续可以统一整理。
- AI 能力依赖单独模型配置；未配置时，AI 拆解会退回基础兜底逻辑。

## 常用命令

```bash
# 前端开发
cd front
npm install
npm run dev

# 前端打包
npm run build

# 后端开发
cd ../end
mvn spring-boot:run

# 后端测试
mvn test

# 后端打包
mvn package
```
