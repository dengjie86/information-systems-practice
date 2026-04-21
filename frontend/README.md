# DormRepair Frontend

校园宿舍报修与工单管理系统的前端项目。

## 技术栈

- Vue 3 + TypeScript + Vite
- Element Plus（UI 组件库）
- Vue Router（路由）
- Pinia（状态管理）
- Axios（HTTP 请求，已在 `utils/request.ts` 封装）
- Sass（样式预处理）

## 目录结构

```text
frontend/
├── src/
│   ├── api/          # 业务接口定义
│   ├── components/   # 公共组件
│   ├── layout/       # 后台整体布局（侧边栏 + Header）
│   ├── router/       # 路由配置与鉴权守卫
│   ├── stores/       # Pinia 状态（用户、Token 等）
│   ├── utils/        # 工具类（Axios 实例等）
│   ├── views/        # 页面
│   │   ├── login/
│   │   ├── home/
│   │   └── placeholder.vue
│   ├── App.vue
│   ├── main.ts
│   └── style.css
├── vite.config.ts    # 开发服务器与 /api 代理
└── package.json
```

## 本地启动

```bash
# 安装依赖（仅首次）
npm install

# 启动开发服务器（默认 3000，被占用时自动递增）
npm run dev

# 打包生产版本
npm run build
```

## 接口代理

开发环境下，所有以 `/api` 开头的请求会被代理到 `http://localhost:8080`（对应后端 Spring Boot 服务）。
如需修改，请编辑 `vite.config.ts` 中的 `server.proxy` 配置。

## 登录默认行为

当前登录接口使用 mock token（任意用户名/密码均可登录），后端登录接口联调后会替换，详见 `views/login/index.vue` 中的 `TODO`。

