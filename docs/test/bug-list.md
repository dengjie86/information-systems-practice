# 缺陷汇总（Bug List）

联调过程中遇到的几个问题和处理情况，记一下方便回顾。详细的复现步骤和修复代码在 `docs/test/接口测试记录.md` 里都有，这里只做表格汇总。

## 1. 总览

到目前为止一共 6 个，都已经处理掉了。

| 编号 | 标题 | 严重程度 | 发现日期 | 发现人 | 状态 | 关联用例 |
| --- | --- | --- | --- | --- | --- | --- |
| BUG-01 | 工单状态流转允许从"待接单"直接跳到"待确认" | 高 | 2026-04-27 | 胡宇翔 | 已修复 | `TC-W-06`、`TC-W-07` |
| BUG-02 | JWT 过期后前端不退出登录，一直 401 弹窗 | 中 | 2026-04-29 | 胡宇翔、尹添一 | 已修复 | `TC-A-08` |
| BUG-03 | 学生取消已驳回工单后状态被错误置为 `CANCELLED` | 中 | 2026-04-30 | 胡宇翔 | 已修复 | `TC-S-15` |
| BUG-04 | mock_data.sql 直接执行报 `Data too long for column 'real_name'` | 低 | 2026-05-04 | 胡宇翔 | 已修复 | 部署/初始化 |
| BUG-05 | JBR (Java 25) + Lombok 1.18.34 编译失败 `cannot find symbol getXxx()` | 低 | 2026-05-04 | 胡宇翔 | 已规避 | 构建环境 |
| BUG-06 | Postman 集合 token 写入 collection 变量被环境空字符串覆盖 | 低 | 2026-05-04 | 胡宇翔 | 已修复 | `TC-A-01~03` |

---

## 2. 详细记录

### BUG-01 工单状态流转允许从"待接单"直接跳到"待确认"

| 项 | 内容 |
| --- | --- |
| 发现时间 | 2026-04-27 14:30 |
| 发现人 | 胡宇翔 |
| 关联用例 | `TC-W-06`、`TC-W-07` |
| 严重程度 | 高 |
| 状态 | 已修复 |
| 修复 commit | `a20b3ce fix: 对齐维修人员接口校验与状态更新逻辑` |
| 修复人 | 邓杰 |

**现象**：维修人员在工单状态为 `PENDING_ACCEPT` 时，跳过"接单"动作直接调用 `POST /api/orders/{id}/finish`，接口返回 200，工单状态被强行置为 `PENDING_CONFIRM`，维修记录中缺失 `ACCEPT` 这一步。

**根因**：`RepairOrderService.finishOrder` 只校验了"操作人是否是分派的维修人员"，缺失了"当前工单状态是否为 `PROCESSING`"的校验。同类问题在 `acceptOrder`、`rejectByWorker`、`addRepairRecord` 中也存在。

**修复**：四个维修人员接口都补了状态校验，详见接口测试记录 §6.1。

**回归**：`TC-W-07` 重跑返回 `409 只有处理中的工单才能完成维修`，`TC-E-01` 端到端主流程跑通。

---

### BUG-02 JWT 过期后前端不退出登录，一直 401 弹窗

| 项 | 内容 |
| --- | --- |
| 发现时间 | 2026-04-29 09:50 |
| 发现人 | 胡宇翔、尹添一 |
| 关联用例 | `TC-A-08` |
| 严重程度 | 中 |
| 状态 | 已修复 |
| 修复 commit | 前端 `utils/request.ts` 拦截器修改（已合入 main） |
| 修复人 | 尹添一 |

**现象**：JWT 过期时间是 2 小时。机器闲置一晚再打开前端，点任意菜单弹一堆"请求失败"红色提示，但页面不会自动跳登录页，本地 token 也没清，必须手动清 localStorage 才能恢复。

**根因**：前端 axios 响应拦截器只对 `code !== 200` 弹 `ElMessage.error`，没有针对 401 的清 token + 路由跳转逻辑；多个并发接口同时 401 时还会叠加多个红色弹窗。

**修复**：在拦截器加 401 分支：清 token、单一提示"登录已失效，请重新登录"、`router.replace('/login')`，详见接口测试记录 §6.2。

**回归**：`TC-A-08` 用伪造 token 调用受保护接口，前端自动跳回登录页，只弹一次提示。

---

### BUG-03 学生取消已驳回工单后状态被错误置为 `CANCELLED`

| 项 | 内容 |
| --- | --- |
| 发现时间 | 2026-04-30 11:20 |
| 发现人 | 胡宇翔 |
| 关联用例 | `TC-S-15` |
| 严重程度 | 中 |
| 状态 | 已修复 |
| 修复 commit | 随 `f1f5859 feat: 实现学生取消报修与评价接口` 后续联调修正一并合入 |
| 修复人 | 邓杰 |

**现象**：工单状态为 `REJECTED`（被管理员驳回）时，前端不显示"取消报修"按钮，但用 Postman 直接打 `POST /api/orders/{id}/cancel`，接口仍然成功把状态改成 `CANCELLED`，覆盖了原来的驳回原因。

**根因**：`cancelOrder` 在查询工单时状态条件过松，只检查了"工单存在且属于当前学生"，没有限制为 `PENDING_AUDIT`。

**修复**：把状态条件改成只允许 `PENDING_AUDIT`，其它状态直接抛 `CONFLICT`，提示"只有待审核工单才可以取消报修"。

**回归**：`TC-S-15` 通过；同时复测 `TC-S-13`（正常取消）、`TC-S-14`（不填原因取消）也都正常。

---

### BUG-04 mock_data.sql 执行时中文字段超长

5/4 联调时初始化数据库，schema.sql 跑通了但 init.sql 报了 `Data too long for column 'real_name'`，mock_data.sql 还报了 `Incorrect string value`。查了一下是 Windows 中文系统下 mysql 客户端默认连接字符集是 latin1，UTF-8 文件进去之后中文按字节计长度就超了。

加个 `--default-character-set=utf8mb4` 参数就好：

```powershell
mysql -u root -p<pwd> --default-character-set=utf8mb4 < database\schema.sql
mysql -u root -p<pwd> --default-character-set=utf8mb4 < database\init.sql
mysql -u root -p<pwd> --default-character-set=utf8mb4 < database\mock_data.sql
```

后面删库重建跑了一遍，三个脚本都跑过了，账号、分类、mock 工单都正确。复现命令在接口测试记录 §9.7 里也列了。

---

### BUG-05 JBR (Java 25) + Lombok 1.18.34 编译失败

本地用 IDEA 2026.1.1 自带的 JBR（JetBrains Runtime，OpenJDK 25）编译 backend，报一堆 `cannot find symbol method getStatus() / getId()` 之类，看起来像 Lombok 没生成 getter/setter。Spring Boot 3.3.5 托管的 Lombok 是 1.18.34，对 Java 25 的支持还不够，annotation processor 在 Java 25 下静默失败了。

项目 pom.xml 里写的就是 Java 21，别用 JBR 跑就行。把 `JAVA_HOME` 切到 `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot` 后编译通过。

避坑提醒：

- IDEA 里：`File → Project Structure → Project SDK` 选 `temurin-21`
- 命令行：`$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"`
- 不要用 IDEA 自带的 jbr 跑 mvn

---

### BUG-06 Postman 集合 token 写入 collection 变量被环境空字符串覆盖

| 项 | 内容 |
| --- | --- |
| 发现时间 | 2026-05-04 15:15 |
| 发现人 | 胡宇翔 |
| 关联用例 | `TC-A-01`、`TC-A-02`、`TC-A-03`（用 Newman 跑 Postman 集合时） |
| 严重程度 | 低 |
| 状态 | 已修复 |
| 修复 commit | `72ddedc fix(test): Postman 集合改用 environment 变量保存 token` |
| 修复人 | 胡宇翔 |

**现象**：导入 `postman_collection.json` + `postman_environment.json` 后，三个登录请求都返回 200 且测试断言通过，但后续所有需要 token 的请求都返回 401。

**复现**：

```powershell
newman run docs/test/postman_collection.json -e docs/test/postman_environment.json --folder "1. 登录与用户"
```

输出会看到：

```
POST /api/auth/login - admin     [200 OK]   √ 登录成功 + tokenAdmin 已写入
GET  /api/user/me                [401 Unauthorized]
GET  /api/user/list              [401 Unauthorized]
...
```

**根因**：原脚本用 `pm.collectionVariables.set('tokenAdmin', ...)` 把 token 写进了 collection 变量。但 Postman 变量解析优先级是 `local > data > environment > collection > global`，环境文件里把 `tokenAdmin` 初始化成空字符串，后续请求里的 `{{tokenAdmin}}` 就被解析成空了，Authorization 头变成 `Bearer `。

**修复**：脚本改成 `pm.environment.set('tokenAdmin', ...)`，让 token 写到环境变量里，覆盖原本的空值。

```diff
-  pm.collectionVariables.set('tokenAdmin', r.data.token);
+  pm.environment.set('tokenAdmin', r.data.token);
```

**回归**：用 Newman 跑了一遍，5 个目录 22 个请求 10 个断言，都过了：

```
0. 健康检查         : 1 requests, 0 failed
1. 登录与用户       : 8 requests, 3 assertions pass
2. 故障分类         : 3 requests, 0 failed
6. 统计接口         : 4 requests, 0 failed
7. 越权与异常       : 6 requests, 7 assertions pass
TOTAL              : 22 requests / 10 assertions / 0 FAILED
```
