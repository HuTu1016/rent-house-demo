# 租房系统后端

基于 JDK 17、Spring Boot 3、MySQL 8 与 Redis 7 的模块化单体后端，服务二手中介房源发布、租客浏览咨询和预约看房。

## 本地启动

1. 复制 `.env.example` 为 `.env`，修改开发环境密码。
2. 执行 `docker compose up -d` 启动 MySQL 与 Redis。
3. 执行 `mvn spring-boot:run -Dspring-boot.run.profiles=local`。

开发热部署：使用 Maven `dev` Profile 启用 `spring-boot-devtools`：`mvn -Pdev spring-boot:run -Dspring-boot.run.profiles=local`。在 IntelliJ IDEA 中将 Maven Profile 设为 `dev`，并勾选 `Settings → Build, Execution, Deployment → Compiler → Build project automatically` 及 `Advanced Settings → Allow auto-make to start even if developed application is currently running`。保存 Java、Mapper XML 或配置文件后，应用会自动重启；生产环境不要启用 `dev` Profile。首次启用需联网下载 DevTools 依赖。

服务地址：`http://localhost:8080`；接口文档：`/swagger-ui/index.html`；健康检查：`/actuator/health`。

本项目不包含合同、退租、支付、账单、报修、评价、审计或房东主体管理。
