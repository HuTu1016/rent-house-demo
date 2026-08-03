# 租房系统后端

基于 JDK 17、Spring Boot 3、MySQL 8 与 Redis 7 的模块化单体后端，服务租客端与房东端完整业务闭环。

## 本地启动

1. 复制 `.env.example` 为 `.env`，修改开发环境密码。
2. 执行 `docker compose up -d` 启动 MySQL 与 Redis。
3. 执行 `mvn spring-boot:run -Dspring-boot.run.profiles=local`。

服务地址：`http://localhost:8080`；接口文档：`/swagger-ui/index.html`；健康检查：`/actuator/health`。

支付仅保留线下付款报备与房东核销流转，不集成第三方支付。
