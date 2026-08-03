# 二手中介租房系统后端需求（v2.0）

## 1. 业务边界

本系统服务对象是二手房租赁中介与看房租客。中介负责采集、维护和发布周边房源，租客负责浏览、收藏、咨询和预约到店/现场看房。

本期只实现以下闭环：

```text
中介创建房屋资产 → 创建房源草稿 → 上传图片/视频 → 发布房源
租客筛选/查看房源 → 收藏/足迹 → 私聊中介 → 提交预约
中介查看会话与预约 → 确认/拒绝/完成带看
租客维护可选身份资料 → 中介在存在会话或预约关系时查看资料
```

明确不属于本系统：房东主体管理、门店主体认证、合同签署、退租、支付、账单、报修、评价、租后服务、审计、财务、在线收款、社区运营和管理员后台。

## 2. 技术与分层

- JDK 17、Spring Boot 3、MySQL 8、Redis 7、Flyway。
- MyBatis-Plus/MyBatis XML 作为唯一持久化方式，不使用 JPA。
- 四层结构按业务模块组织，每个模块固定使用 `controller`、`service`、`mapper`、`entity`，并将接口模型放入 `dto`、`vo`，状态枚举放入 `enums`：
  - `controller`：HTTP 参数校验、权限和响应包装。
  - `service`：业务规则、事务和状态流转；禁止被 Controller 之外的层绕过。
  - `mapper`：MyBatis 接口与 XML，只负责数据库读写。
  - `entity`：数据库实体和值对象；`dto` 为请求模型，`vo` 为响应模型，`enums` 为稳定状态码。

示例目录：`com.renthouse.listing/{controller,entity,enums,mapper,service,dto,vo}`。`common` 仅存放全局响应、异常、配置和基础设施，不承载业务逻辑。
- Controller 禁止直接使用 `JdbcTemplate` 或拼接 SQL。
- 金额统一整数分；对外 ID 统一字符串；敏感资料使用密文列和脱敏列。
- 中介角色代码为 `AGENT`，租客角色代码为 `TENANT`；数据库业务列使用 `agent_id`。

## 3. 角色权限

### 3.1 中介

- 创建楼栋/小区和房屋单元。
- 创建、编辑、发布、下架房源。
- 管理房源图片、视频、封面、排序和特价展示。
- 查看租客会话、发送消息。
- 查看预约、确认、拒绝、标记完成带看。
- 查看与其存在会话或预约关系的租客身份资料。

### 3.2 租客

- 公开浏览和筛选房源。
- 查看房源详情、图片、视频、设施与费用说明。
- 收藏、取消收藏、查看足迹、最多三套对比。
- 发起按房源隔离的私聊。
- 提交、取消预约，查看预约状态。
- 填写姓名、身份证号、手机号；家庭住址、公司名称、公司地址为选填。

## 4. 核心状态

- 房源发布：`DRAFT`、`PUBLISHED`、`OFFLINE`。
- 房屋占用：`VACANT`、`PENDING_VIEWING`、`OCCUPIED`、`MAINTENANCE`。
- 会话：`NORMAL`、`CLOSED`；拉黑和租后履约不在本期。
- 预约：`PENDING`、`CONFIRMED`、`DECLINED`、`CANCELLED`、`COMPLETED`。
- 资料：`INCOMPLETE`、`COMPLETED`。

## 5. 接口契约

统一前缀 `/api/v1`，响应格式为 `{code,message,data,traceId}`。

### 5.1 认证

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/password/login` | 中介账号登录 |
| POST | `/auth/refresh` | 刷新访问令牌 |

### 5.2 租客资料

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tenant/profile` | 当前租客资料和收藏/足迹数量 |
| PATCH | `/tenant/profile` | 更新身份资料 |
| GET | `/agent/tenants/{tenantId}/profile` | 中介查看有关系租客资料 |

身份证号、手机号不得直接返回明文；身份证返回脱敏值，手机号按权限返回脱敏或受控值。

### 5.3 公开房源

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/tenant/home` | 特价房和首屏统计聚合 |
| GET | `/listings` | 关键词、区域、户型、租金、标签和排序分页 |
| GET | `/listings/{listingId}` | 房源详情与媒体 |
| PUT/DELETE | `/tenant/favorites/{listingId}` | 收藏/取消收藏 |
| GET | `/tenant/favorites` | 收藏列表 |
| GET | `/tenant/history` | 浏览足迹 |

### 5.4 中介房源管理

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/agent/properties/buildings` | 创建楼栋/小区 |
| POST | `/agent/properties/units` | 创建房屋单元 |
| POST | `/agent/listings` | 创建房源草稿 |
| GET | `/agent/listings` | 查询本人房源 |
| POST | `/agent/listings/{id}/publish` | 发布房源 |
| POST | `/agent/listings/{id}/offline` | 下架房源 |
| PATCH | `/agent/listings/{id}/special` | 设置特价和排序 |
| POST | `/agent/listings/{id}/media` | 添加图片/视频媒体 |
| POST | `/files/upload` | 本地演示上传，未来可替换 MinIO |

### 5.5 会话与预约

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/tenant/listings/{listingId}/conversation` | 获取或创建房源会话 |
| GET | `/conversations` | 租客/中介会话列表 |
| GET | `/conversations/{id}/messages` | 会话消息 |
| POST | `/conversations/{id}/messages` | 发送文本消息 |
| POST | `/tenant/listings/{listingId}/appointments` | 租客提交预约 |
| GET | `/appointments` | 按当前角色查询预约 |
| PATCH | `/appointments/{id}/status` | 中介确认/拒绝/完成，租客取消 |

## 6. 数据库表

### 6.1 用户与资料

- `sys_user`：账号、角色、登录状态。
- `tenant_profile`：租客基础扩展资料。
- `tenant_identity_profile`：姓名、身份证密文/脱敏值、手机号密文/哈希、家庭住址、公司资料。

### 6.2 房源

- `property_building`：小区/楼栋和中介归属。
- `property_unit`：具体房屋、户型、面积、占用状态。
- `house_listing`：展示标题、区域、租金、标签、设施、发布状态和特价状态。
- `listing_media`：图片、视频、封面、排序和外链/文件地址。

### 6.3 租客互动

- `tenant_favorite`：收藏唯一约束。
- `tenant_browse_history`：按租客+房源更新最后浏览时间。
- `conversation`：房源+租客+中介唯一会话。
- `chat_message`：文本和预约卡片消息。
- `appointment`：预约时间、联系人、状态和拒绝原因。

### 6.4 文件

当前演示使用本地文件目录；数据库只需在后续接入对象存储时增加 `file_object`，不引入支付、账单或审计表。

## 7. 事务规则

1. 发布房源必须属于当前中介且房屋可用。
2. 同一房源、同一租客、同一中介只能有一个会话。
3. 预约必须从已发布且可看房房源创建。
4. 只有会话成员可以读写消息。
5. 中介只能查看与自己存在会话或预约关系的租客身份资料。
6. 收藏重复调用幂等；足迹重复浏览更新时间，不重复插入。
7. 预约状态只能按状态机转换，拒绝必须填写原因。

## 8. 验收

- `npm run build` 通过。
- `mvn -s .mvn/settings.xml test` 通过。
- Flyway 在全新 MySQL 数据库执行成功。
- `V2/V3` 为未上线前的历史迁移占位版本，已不再写入演示数据；若已有环境执行过旧版本迁移，发布前需清理旧 Flyway history 后在正式数据库重新迁移，禁止直接修改已发布迁移的 checksum。
- 演示链路：中介创建房屋→发布房源→租客浏览→私聊→预约→中介确认→完成带看。
- 不出现合同、退租、支付、账单、报修、评价、审计、门店主体和房东角色接口。
