# 项目协作与任务管理系统

一个基于 Vue 3 + Vite + Spring Boot + MyBatis + MySQL 的轻量级协作平台，覆盖项目管理、任务管理、进度跟踪、总结中心、成员管理，以及 AI 辅助任务拆解。

## 项目结构

```text
Web/
├─ front/   前端，Vue 3 + Vite
└─ end/     后端，Spring Boot + MyBatis
```

## 技术栈

- 前端：Vue 3、Vite
- 后端：Spring Boot 4、MyBatis
- 数据库：MySQL 8.x
- 鉴权：基于请求头 `token` 的 JWT
- 构建工具：npm、Maven
- 可选能力：AI 项目拆解与任务建议

## 核心功能

- 登录鉴权与角色控制
- 项目新增、编辑、删除、列表查询
- 任务新增、编辑、删除、状态管理
- 进度记录新增、编辑、删除
- 总结中心查看与新增
- 成员列表与个人信息维护
- AI 辅助项目拆解，支持将建议任务导入项目
- 主要业务页面统一支持分页

## 当前角色说明

后端角色定义位于 `end/src/main/java/com/example/end/config/UserRoleConfig.java`：

- `0`：管理员
- `1`：项目负责人
- `2`：普通成员

结合当前代码，权限行为大致如下：

- 管理员可以查看全局数据，但前端不显示“新增任务”“新增记录”“新增总结”按钮。
- 成员列表页面只有管理员可操作。
- 成员列表接口会过滤管理员，普通成员不会出现在成员列表中看到管理员数据。
- 项目负责人可以查看自己负责的项目。
- 普通成员在任务管理、进度跟踪等页面主要查看与自己相关的数据。
- 总结中心对所有人开放查看，前端交互权限已统一。

说明：具体数据过滤仍以接口实现为准，建议同时参考各 Controller 中的权限判断逻辑。

## 页面与交互特性

- 新增、编辑等主要操作已改为按钮触发的弹窗式交互。
- 编辑弹窗会对当前选中数据进行预填。
- 删除操作已统一改为自定义确认弹窗，不再使用浏览器 `alert/confirm`。
- 所有主要列表页支持分页，每页可切换 `5 / 10 / 12` 条。
- 总览页中的“我的待办”当前不分页。

## 环境要求

- Node.js `^20.19.0 || >=22.12.0`
- JDK 17
- Maven 3.9+
- MySQL 8.x

## 数据库准备

先创建数据库：

```sql
CREATE DATABASE mini_semester CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

后端启动时会自动执行：

- `end/src/main/resources/schema.sql`
- `end/src/main/resources/data.sql`

也就是说，表结构与演示数据会自动初始化。

## 后端配置

当前配置文件：`end/src/main/resources/application.properties`

默认关键配置如下：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mini_semester?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=230038
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

jwt.secret=mini-semester-jwt-secret-change-me
jwt.expire-seconds=86400
```

如果本地数据库用户名、密码或库名不同，先修改这里再启动。

## 可选 AI 配置

后端会尝试按下面这行配置加载 AI 配置文件：

```properties
spring.config.import=optional:classpath:application-ai.properties
```

没有该文件也可以正常启动，只是 AI 相关能力不可用，或回退为基础兜底逻辑。

可在 `end/src/main/resources/` 下新增 `application-ai.properties`：

```properties
ai.base-url=你的模型服务地址
ai.api-key=你的密钥
ai.model=你的模型名称
```

## 启动方式

### 启动后端

在 `end/` 目录执行：

```bash
mvn spring-boot:run
```

或者先打包再运行：

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

### 前端打包

```bash
cd front
npm run build
```

产物目录：

- `front/dist`

### 后端打包

```bash
cd end
mvn package
```

产物目录：

- `end/target/end-0.0.1-SNAPSHOT.jar`

## 测试

后端测试：

```bash
cd end
mvn test
```

说明：`mvn package` 过程中也会执行测试。

## 默认演示数据

初始化数据来自 `end/src/main/resources/data.sql`，默认密码均为 `123456`。

默认账号包括：

- 管理员：`admin`
- 项目负责人：`zhangchen`、`lixue`、`wanghao`
- 普通成员：`zhaomin`、`qiankun`、`sunyue`、`zhouning`、`wudi`、`zhengjia`

当前初始化数据还包含：

- 3 个项目
- 6 个任务
- 3 条进度记录
- 3 条总结记录

## 主要接口

- `POST /auth/login`
- `GET/POST/PUT/DELETE /sysUsers`
- `GET/POST/PUT/DELETE /projectInfos`
- `GET/POST/PUT/DELETE /taskInfos`
- `GET/POST/PUT/DELETE /taskLogs`
- `GET/POST/PUT/DELETE /taskSummaries`
- `POST /api/ai/task-suggestion`
- `POST /api/ai/project-breakdown`

除 `/auth/login` 外，其余接口默认都需要在请求头中携带 `token`。

## 目录说明

前端核心目录：

- `front/src/components/workspace/`：页面与弹窗组件
- `front/src/composables/useWorkspaceApp.js`：主要状态与业务逻辑
- `front/src/components/workspace/PaginationControls.vue`：分页组件
- `front/src/components/workspace/ActionModal.vue`：统一操作弹窗

后端核心目录：

- `end/src/main/java/com/example/end/controller/`：接口层
- `end/src/main/java/com/example/end/service/`：业务层
- `end/src/main/java/com/example/end/mapper/`：MyBatis 持久层
- `end/src/main/java/com/example/end/auth/`：登录与 JWT
- `end/src/main/java/com/example/end/agent/`：AI 任务建议与项目拆解

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

## 注意事项

- `application.properties` 中当前写的是本地开发数据库账号密码，部署前建议改为环境变量或外部配置。
- AI 能力依赖额外模型配置，未配置时不会影响基础业务功能。
- 历史 README 曾存在编码问题，当前版本已统一整理为可直接阅读的说明。
