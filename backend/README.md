# 后端服务

## 技术栈

- Spring Boot 3
- MyBatis-Plus
- MySQL 8
- JWT
- Lombok
- Maven
- JDK 21

## 目录结构

```text
backend
|- src/main/java/com/dormrepair
|  |- common
|  |- config
|  |- security
|  |- auth
|  |- user
|  |- order
|  |- category
|  |- record
|  |- evaluation
|  |- file
|  `- stats
`- src/main/resources
```

## 数据库准备

开发环境默认连接本地 MySQL 数据库 `dorm_repair`。

启动后端前，请先在项目根目录执行数据库脚本：

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p dorm_repair < database/init.sql
```

默认开发环境配置：

- 数据库地址：`jdbc:mysql://localhost:3306/dorm_repair`
- 用户名：`root`
- 密码：`123456`

也可以通过环境变量覆盖数据库账号：

- `DORM_DB_USERNAME`
- `DORM_DB_PASSWORD`

## 启动方式

1. 确认 `JAVA_HOME` 指向 JDK 21。
2. 确认 MySQL 8 已启动，并且数据库脚本已经执行。
3. 在 `backend/` 目录下执行：

```bash
mvn spring-boot:run
```

也可以直接运行启动类：

```text
com.dormrepair.DormRepairApplication
```

启动后可访问健康检查接口：

```http
GET /api/ping
```

## 当前已实现接口

### 健康检查

```http
GET /api/ping
```

### 用户登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

登录成功后会返回 JWT token 和当前用户信息。

### 当前登录用户

```http
GET /api/user/me
Authorization: Bearer <token>
```

该接口需要在请求头中携带登录接口返回的 token。

## 初始化测试账号

初始化账号密码均为 `123456`。

| 用户名 | 角色 | 说明 |
| --- | --- | --- |
| `admin` | `ADMIN` | 系统管理员 |
| `student1` | `STUDENT` | 学生账号 |
| `worker1` | `WORKER` | 维修人员账号 |

## 测试

在 `backend/` 目录下执行：

```bash
mvn test
```

当前认证流程测试使用 `test` 配置和内存 H2 数据库，不依赖本地 MySQL。
