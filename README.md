# 校园宿舍报修与工单管理系统

## 项目简介

本项目是一个面向高校宿舍后勤场景的报修与工单管理系统，覆盖学生提交报修、管理员审核与分派、维修人员处理、学生评价反馈和数据统计等流程。当前版本已经完成“学生报修 + 管理员审核分派”的可演示主流程，维修端、评价、图片上传和统计分析仍在后续开发范围内。

## 技术栈

- 后端：Spring Boot 3、MyBatis-Plus、MySQL 8、JWT、Maven、JDK 21
- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router、Axios
- 测试：JUnit 5、Spring Boot Test、H2 MySQL 模式

## 团队成员与分工

| 姓名 | Git 账号 | 负责角色 | 核心工作内容 |
| :--- | :--- | :--- | :--- |
| **邓杰** | dengjie86 | 项目负责人 / 后端主程 | 系统架构、登录认证、权限控制、工单核心业务逻辑 |
| **尹添一** | gubei026314 | 前端主程 | 登录页、学生端、管理端页面开发，前后端接口联调与页面优化 |
| **陈梓轩** | yihai23 | 数据库与后端协助 | 数据库设计、实体与 Mapper、统计分析接口 |
| **胡宇翔** | saintwugo | 测试与文档负责人 | 测试用例、测试记录、计划书、中期报告、软件说明书、最终演示材料 |

## 目录结构

```text
.
|- backend/    # Spring Boot 后端服务
|- frontend/   # Vue 前端项目
|- database/   # 建表、初始化、演示数据和迁移脚本
|- docs/       # 接口规范、数据库设计、项目计划等文档
`- README.md
```

## 快速启动

### 1. 初始化数据库

首次创建本地数据库时，在项目根目录执行：

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p dorm_repair < database/init.sql
mysql -u root -p dorm_repair < database/mock_data.sql
```

如果本地已经有旧版 `dorm_repair` 数据库，需要额外执行迁移脚本：

```bash
mysql -u root -p dorm_repair < database/migrations/20260428_add_dispatch_remark.sql
```

该迁移会给 `repair_order` 表补充 `dispatch_remark` 字段。缺少该字段时，管理端工单列表会出现 `Unknown column 'dispatch_remark'` 异常。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认后端地址：`http://localhost:8080`

健康检查：`http://localhost:8080/api/ping`

默认数据库连接配置见 `backend/src/main/resources/application-dev.yml`：

- 数据库：`jdbc:mysql://localhost:3306/dorm_repair`
- 用户名：`DORM_DB_USERNAME`，默认 `root`
- 密码：`DORM_DB_PASSWORD`，默认 `123456`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认前端地址：`http://localhost:3000`

开发环境下，前端 `/api` 请求会通过 Vite 代理转发到 `http://localhost:8080`。

## 测试账号

初始化脚本中的账号密码均为 `123456`。

| 账号 | 角色 | 说明 |
| :--- | :--- | :--- |
| `admin` | `ADMIN` | 管理员账号 |
| `student1` | `STUDENT` | 学生账号 |
| `worker1` | `WORKER` | 维修人员账号 |

导入 `database/mock_data.sql` 后，会包含更多演示工单、学生和维修人员数据。

## 当前已实现功能

### 通用能力

- 登录认证与 JWT 鉴权
- 前端路由按角色控制访问
- 统一接口响应结构
- 业务异常按 HTTP 状态码返回

### 学生端

- 维护宿舍楼、宿舍号和联系电话
- 查看启用中的故障分类
- 提交报修工单
- 查看我的工单列表
- 查看工单详情、状态、驳回原因和管理员备注

### 管理端

- 查看工单列表与详情
- 审核通过待审核工单
- 驳回待审核工单并填写驳回原因
- 分派已审核工单给维修人员并填写分派备注
- 查看用户列表
- 新增、编辑、启用、停用故障分类
- 删除未被工单使用的故障分类

## 当前开发进度

- [x] 项目计划书定稿与提交
- [x] Git 仓库目录结构初始化
- [x] 数据库表结构设计
- [x] 后端基础架构搭建
- [x] 前端基础架构搭建
- [x] 统一响应体与接口规范
- [x] 登录认证与 JWT 鉴权
- [x] 前端登录与路由鉴权流程
- [x] 用户信息与故障分类模块
- [x] 学生报修提交与我的工单查询
- [x] 学生端报修与工单页面开发
- [x] 管理员工单审核与分派功能
- [x] 管理端工单处理页面开发
- [x] 管理端用户管理与故障分类维护页面
- [ ] 维修人员接单处理与维修记录
- [ ] 维修端接单处理页面开发
- [ ] 工单确认完成与评价反馈流程
- [ ] 报修与维修图片上传功能
- [ ] 管理端统计分析接口与图表
- [ ] 接口测试记录与联调 Bug 修复
- [ ] 中期检查材料与演示数据准备
- [ ] 系统主流程交付与最终说明文档

## 验证命令

后端测试：

```bash
cd backend
mvn test
```

前端构建：

```bash
cd frontend
npm run build
```

当前后端测试会使用 `backend/src/test/resources/schema-h2.sql` 创建内存 H2 测试库，用来验证登录、用户分类、学生报修、管理员审核分派和数据库结构同步，不依赖本地 MySQL。

## 关键文档

- `docs/api/接口规范.md`：接口路径、请求参数和响应格式
- `docs/数据库设计.md`：数据库表结构说明
- `database/schema.sql`：生产库建表脚本
- `database/init.sql`：基础账号和分类初始化脚本
- `database/mock_data.sql`：演示数据脚本
- `database/migrations/20260428_add_dispatch_remark.sql`：旧库升级迁移脚本
