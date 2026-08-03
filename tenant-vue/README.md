# 房乐管租客端（Vue 3）

独立 Vue 3 + Vite + TypeScript 租客端，业务范围为房源浏览、收藏足迹、私聊、预约看房和租客身份资料。所有业务数据均来自 Spring Boot API，不内置本地演示数据。

## 开发

```bash
npm install
npm run dev
```

## 与 Spring Boot 对接

Pinia Store 只负责页面状态和接口结果缓存；API 地址通过 `VITE_API_BASE_URL` 配置。本端不包含合同、支付、账单、报修、评价和租后服务页面。

## 联调

复制 `.env.example` 为 `.env.local` 后启动后端和前端：

```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_TENANT_MOBILE=租客账号手机号
VITE_TENANT_PASSWORD=租客账号密码
```

前端启动时使用配置的租客账号登录，并加载房源、收藏、足迹、预约和身份资料；收藏、私聊、预约和身份资料保存会写入 Spring Boot API。未配置账号或后端不可用时只显示错误提示，不回退到本地假数据。
