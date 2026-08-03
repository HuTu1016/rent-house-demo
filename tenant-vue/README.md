# 房乐管租客端（Vue 3）

独立 Vue 3 + Vite + TypeScript 租客端演示工程。页面默认使用 Mock 数据，刷新浏览器会恢复标准演示场景。

## 开发

```bash
npm install
npm run dev
```

## 与 Spring Boot 对接

当前数据集中在 Pinia Store 与 `src/data.ts`。后续以 `VITE_API_MODE` 和 `VITE_API_BASE_URL` 切换服务实现，页面组件与路由不直接依赖后端接口。
