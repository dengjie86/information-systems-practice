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

启动后端前，先在项目根目录执行：

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p dorm_repair < database/init.sql
```

如果要导入演示工单数据，再执行：

```bash
mysql -u root -p dorm_repair < database/mock_data.sql
```

默认开发环境配置：

- 数据库地址：`jdbc:mysql://localhost:3306/dorm_repair`
- 用户名：`root`
- 密码：`123456`

也可以通过环境变量覆盖：

- `DORM_DB_USERNAME`
- `DORM_DB_PASSWORD`

## 启动方式

1. 确认 `JAVA_HOME` 指向 JDK 21
2. 确认 MySQL 8 已启动，且数据库脚本已执行
3. 在 `backend/` 目录下执行：

```bash
mvn spring-boot:run
```

也可以直接运行启动类：

```text
com.dormrepair.DormRepairApplication
```

启动后可先访问：

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

登录成功后返回 JWT token 和当前登录用户信息。

### 当前登录用户

```http
GET /api/user/me
Authorization: Bearer <token>
```

### 学生修改宿舍信息

```http
PUT /api/user/dorm
Authorization: Bearer <token>
Content-Type: application/json

{
  "dormBuilding": "2号楼",
  "dormRoom": "305",
  "phone": "13800000099"
}
```

### 管理员查看用户列表

```http
GET /api/user/list
Authorization: Bearer <token>
```

### 管理员查看维修人员列表

```http
GET /api/user/workers
Authorization: Bearer <token>
```

### 故障分类列表

学生提交报修时使用，只返回启用中的分类：

```http
GET /api/category/list
Authorization: Bearer <token>
```

### 管理员查看全部故障分类

```http
GET /api/category/all
Authorization: Bearer <token>
```

### 管理员新增故障分类

```http
POST /api/category/add
Authorization: Bearer <token>
Content-Type: application/json

{
  "categoryName": "网络故障",
  "description": "网络断连",
  "sortOrder": 3
}
```

### 管理员修改故障分类

```http
PUT /api/category/update
Authorization: Bearer <token>
Content-Type: application/json

{
  "id": 2,
  "categoryName": "门窗维修",
  "description": "窗户损坏",
  "sortOrder": 2
}
```

### 管理员启用或停用故障分类

```http
PUT /api/category/{id}/status?status=1
Authorization: Bearer <token>
```

`status=1` 表示启用，`status=0` 表示停用。

### 学生提交报修工单

```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "空调漏水",
  "categoryId": 1,
  "description": "床边一直滴水",
  "imageUrl": "/img/kongtiao.png",
  "contactPhone": "13800000999",
  "priority": "URGENT"
}
```

说明：

- 仅学生可以提交
- 提交前必须先维护宿舍信息
- 故障分类必须存在且处于启用状态
- 新工单默认状态为 `PENDING_AUDIT`

### 学生查看我的工单

```http
GET /api/orders/my?pageNum=1&pageSize=10
Authorization: Bearer <token>
```

可选筛选参数：

- `status`

### 学生查看工单详情

```http
GET /api/orders/{id}
Authorization: Bearer <token>
```

当前会返回工单编号、分类、宿舍、图片、状态、驳回原因、管理员备注等字段。

## 初始测试账号

初始化账号密码均为 `123456`。

| 用户名 | 角色 | 说明 |
| --- | --- | --- |
| `admin` | `ADMIN` | 系统管理员 |
| `student1` | `STUDENT` | 学生账号 |
| `worker1` | `WORKER` | 维修人员账号 |

如果导入了 `mock_data.sql`，还会有更多学生和维修人员测试数据。

## 测试

在 `backend/` 目录下执行全部测试：

```bash
mvn test
```

如果只验证当前登录、用户分类、学生报修这几部分：

```bash
mvn -q "-Dtest=AuthFlowIntegrationTest,UserCategoryIntegrationTest,StudentRepairOrderIntegrationTest" test
```

当前测试使用 `test` 配置和内存 H2 数据库，不依赖本地 MySQL。

## 手工联调建议

建议按下面顺序验证：

1. `POST /api/auth/login`
2. `GET /api/user/me`
3. `PUT /api/user/dorm`
4. `GET /api/category/list`
5. `POST /api/orders`
6. `GET /api/orders/my`
7. `GET /api/orders/{id}`

重点检查：

- token 是否能正常鉴权
- 学生是否只能查看自己的工单
- 停用分类是否无法提交
- 工单创建后状态是否为 `PENDING_AUDIT`
