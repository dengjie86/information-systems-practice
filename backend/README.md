# 后端服务

校园宿舍报修与工单管理系统后端，基于 Spring Boot 3 + MyBatis-Plus，实现了登录鉴权、用户信息维护、故障分类管理、学生报修、管理员审核分派、维修员处理与维修记录查询等能力。

## 技术栈

- Spring Boot 3
- MyBatis-Plus
- MySQL 8
- H2
- JWT
- Maven
- JDK 21

## 目录结构

```text
backend
|- src/main/java/com/dormrepair
|  |- auth
|  |- category
|  |- common
|  |- config
|  |- evaluation
|  |- file
|  |- order
|  |- record
|  |- security
|  |- stats
|  `- user
|- src/main/resources
|  |- application-dev.yml
|  |- application-local.yml
|  |- schema-h2.sql
|  `- data-h2.sql
`- src/test
```

## 运行环境

- JDK 21
- Maven 3.9+
- MySQL 8（开发环境可选）

## 启动方式

### 方式一：开发环境使用 MySQL

默认开发配置见 [application-dev.yml](/D:/Workspace/校园宿舍保修与工单管理系统/backend/src/main/resources/application-dev.yml:1)。

1. 初始化数据库：

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p dorm_repair < database/init.sql
```

2. 如需演示数据，可继续导入：

```bash
mysql -u root -p dorm_repair < database/mock_data.sql
```

3. 默认数据库连接：

- URL：`jdbc:mysql://localhost:3306/dorm_repair`
- 用户名：`root`
- 密码：`${DORM_DB_PASSWORD:123456}`，未设置环境变量时默认 `123456`

4. 可通过环境变量覆盖：

- `DORM_DB_USERNAME`
- `DORM_DB_PASSWORD`

5. 启动：

```bash
cd backend
mvn spring-boot:run
```

### 方式二：本地联调使用内存 H2

当前仓库已经提供本地 H2 配置和初始化数据：

- [application-local.yml](/D:/Workspace/校园宿舍保修与工单管理系统/backend/src/main/resources/application-local.yml:1)
- [schema-h2.sql](/D:/Workspace/校园宿舍保修与工单管理系统/backend/src/main/resources/schema-h2.sql:1)
- [data-h2.sql](/D:/Workspace/校园宿舍保修与工单管理系统/backend/src/main/resources/data-h2.sql:1)

使用本地 profile 启动：

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

这个模式不依赖本机 MySQL，适合快速联调接口或演示基础流程。

### 启动后验证

```http
GET /api/ping
```

## 认证说明

除登录和健康检查外，其余接口都需要在请求头携带 JWT：

```http
Authorization: Bearer <token>
```

## 角色说明

- `STUDENT`：学生
- `ADMIN`：管理员
- `WORKER`：维修员

## 工单状态

- `PENDING_AUDIT`：待审核
- `PENDING_ASSIGN`：待分派
- `REJECTED`：已驳回
- `PENDING_ACCEPT`：待接单
- `PROCESSING`：处理中
- `PENDING_CONFIRM`：待确认
- `COMPLETED`：已完成
- `CLOSED`：已关闭

当前后端已完整实现到 `PENDING_CONFIRM` 的主流程。`COMPLETED`、`CLOSED` 状态常量已定义，后续可继续扩展确认/评价闭环。

## 当前已实现接口

### 健康检查

```http
GET /api/ping
```

### 登录

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 当前登录用户

```http
GET /api/user/me
Authorization: Bearer <token>
```

### 学生维护宿舍信息

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

### 管理员查看维修员列表

```http
GET /api/user/workers
Authorization: Bearer <token>
```

### 分类接口

学生报修使用启用中的分类：

```http
GET /api/category/list
Authorization: Bearer <token>
```

管理员查看全部分类：

```http
GET /api/category/all
Authorization: Bearer <token>
```

管理员新增分类：

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

管理员修改分类：

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

管理员启用/停用分类：

```http
PUT /api/category/{id}/status?status=1
Authorization: Bearer <token>
```

管理员删除未使用分类：

```http
DELETE /api/category/{id}
Authorization: Bearer <token>
```

### 学生工单接口

创建报修工单：

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

约束：

- 仅学生可提交
- 提交前必须先维护宿舍信息
- 分类必须存在且为启用状态
- `priority` 仅支持 `LOW|NORMAL|HIGH|URGENT`
- 新工单初始状态为 `PENDING_AUDIT`

查看我的工单：

```http
GET /api/orders/my?pageNum=1&pageSize=10&status=PENDING_AUDIT
Authorization: Bearer <token>
```

查看工单详情：

```http
GET /api/orders/{id}
Authorization: Bearer <token>
```

当前详情返回：

- 工单基础信息
- 宿舍、联系方式、时间字段
- `adminRemark`
- 学生可见的 `rejectReason`
- `records` 维修记录列表

说明：

- 学生只能查看自己的工单
- 维修员只能查看分配给自己的工单
- `rejectReason` 仅在工单状态为 `REJECTED` 时对学生端可见

### 管理员工单接口

分页查看工单：

```http
GET /api/orders/admin?pageNum=1&pageSize=10&status=PENDING_ASSIGN&userId=1&assignedWorkerId=3
Authorization: Bearer <token>
```

查看工单详情：

```http
GET /api/orders/admin/{id}
Authorization: Bearer <token>
```

审核通过：

```http
POST /api/orders/admin/{id}/approve
Authorization: Bearer <token>
Content-Type: application/json

{
  "adminRemark": "信息已核实"
}
```

驳回工单：

```http
POST /api/orders/admin/{id}/reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "rejectReason": "宿舍号和故障描述不完整"
}
```

分派维修员：

```http
POST /api/orders/admin/{id}/assign
Authorization: Bearer <token>
Content-Type: application/json

{
  "workerId": 3,
  "dispatchRemark": "今晚前上门处理"
}
```

说明：

- `dispatchRemark` 也兼容旧字段名 `adminRemark`
- 仅管理员可调用
- 分派时会清理前一次拒单留下的 `rejectReason`
- 管理员详情接口会返回完整 `records` 维修记录

### 维修员工单接口

接单：

```http
POST /api/orders/{id}/accept
Authorization: Bearer <token>
Content-Type: application/json

{
  "remark": "10分钟后到宿舍"
}
```

拒单：

```http
POST /api/orders/{id}/worker-reject
Authorization: Bearer <token>
Content-Type: application/json

{
  "rejectReason": "当前不在校内"
}
```

补充维修记录：

```http
POST /api/orders/{id}/record
Authorization: Bearer <token>
Content-Type: application/json

{
  "actionDesc": "已更换灯管并测试通电",
  "resultImage": "/img/repair-result-1.png"
}
```

完成维修：

```http
POST /api/orders/{id}/finish
Authorization: Bearer <token>
Content-Type: application/json

{
  "actionDesc": "已完成更换并恢复正常使用",
  "resultImage": "/img/repair-result-final.png"
}
```

说明：

- 仅维修员本人可操作分配给自己的工单
- `accept` 只允许 `PENDING_ACCEPT`
- `worker-reject` 只允许 `PENDING_ACCEPT`
- `record` 只允许 `PROCESSING`
- `finish` 只允许 `PROCESSING`
- `finish.actionDesc` 必填
- 以上动作都会写入 `repair_record`

## 维修记录返回结构

工单详情中的 `records` 字段结构如下：

```json
[
  {
    "id": 1,
    "orderId": 4,
    "workerId": 3,
    "actionType": "RECORD",
    "actionDesc": "已更换配件",
    "resultImage": "/img/result.png",
    "statusBefore": "PROCESSING",
    "statusAfter": "PROCESSING",
    "actionTime": "2026-04-29 10:00:00"
  }
]
```

当前 `actionType` 可能出现：

- `ACCEPT`
- `REJECT`
- `RECORD`
- `FINISH`

## 初始测试账号

默认密码均为 `123456`。

### 开发库 / 初始化脚本常用账号

- `admin`：管理员
- `student1`：学生
- `worker1`：维修员

### 本地 H2 profile 示例账号

- `admin`
- `student1`
- `student2`
- `worker1`

## 测试

在 `backend/` 目录下执行：

```bash
mvn test
```

当前集成测试覆盖：

- `AuthFlowIntegrationTest`
- `DatabaseSchemaIntegrationTest`
- `UserCategoryIntegrationTest`
- `StudentRepairOrderIntegrationTest`
- `AdminRepairOrderIntegrationTest`

测试默认使用 H2，不依赖本机 MySQL。

## 手工联调建议

建议按下面顺序联调：

1. `POST /api/auth/login`
2. `GET /api/user/me`
3. `PUT /api/user/dorm`
4. `GET /api/category/list`
5. `POST /api/orders`
6. `POST /api/orders/admin/{id}/approve`
7. `POST /api/orders/admin/{id}/assign`
8. `POST /api/orders/{id}/accept`
9. `POST /api/orders/{id}/record`
10. `POST /api/orders/{id}/finish`
11. `GET /api/orders/{id}`
12. `GET /api/orders/admin/{id}`

重点检查：

- token 是否生效
- 角色权限是否正确
- 工单状态流转是否符合预期
- 学生端是否只看到自己可见的字段
- 管理端详情里的 `records` 是否完整
