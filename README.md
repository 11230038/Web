1. 启动后端服务：

```bash
java -jar end-0.0.1-SNAPSHOT.jar
```

2. 启动前端 Nginx：

```bash
cd nginx-1.22.1-Pacal
start nginx.exe
```

3. 浏览器访问：

```text
http://localhost
```

如需停止前端 Nginx：

```bash
cd nginx-1.22.1-Pacal
nginx.exe -s stop


数据库密码是123456，架构名是mini_semester,端口是3306