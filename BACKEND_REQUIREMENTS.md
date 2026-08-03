# 租房平台后端需求与技术设计文档

> 文档版本：v1.1（二次页面核对优化版）  
> 文档状态：开发前评审版  
> 需求基线：`demo.html`、`tenant-vue` 租客端实现、现有产品需求说明  
> 技术基线：JDK 17、Spring Boot 3.x、MySQL 8.0、Redis 7.x  
> 接口前缀：`/api/v1`  
> 文档目标：在不编写后端业务代码的前提下，明确开发范围、接口契约、数据模型、状态机、权限、安全、测试和上线前置条件。

---

## 0. 二次核对结论与实现基线

本版已逐项复核 `demo.html` 的房东端 8 个后台模块、租客端全部 Tab/弹窗，以及 `tenant-vue` 的 6 个路由页面。结论如下：

- 租客 Vue 当前实现是演示交互子集，`demo.html` 和产品需求说明中的房东处理动作是闭环的另一半；后端必须同时支持两端，不能只按 Vue Store 的字段建表。
- Vue 路由中的 `houseId` 在后端统一解释为 `listingId`。前端可继续沿用路由名，但所有 API 和数据库统一使用“房源 `listing`”术语。
- Vue 中浏览数、发布时间、评分、门店资料、合同和用户资料存在硬编码；接入后端后必须全部由接口返回，不能继续保留页面常量作为真实业务数据。
- 原型把“空置/已出租/即将到期”和“草稿/上架/下架”混为同一状态。后端必须拆分“物理房屋占用状态”和“房源发布状态”，详见 4.1。
- 原型中“生成合同”直接把房源设为已出租。正式接口允许一键完成，但必须在单事务中校验租客、房源占用、租期、费用和合同附件，不能只改一个状态值。
- 小程序页面要求图文账单和图文报修；账单图片、表读数照片、付款凭证、报修前后照片是四类不同附件，必须分别建模和鉴权。
- “快速回复”“拨打电话”“视频播放”“横滑画廊”“筛选面板开关”属于前端交互；后端只提供模板数据、公开电话、媒体元数据、筛选数据和必要的行为统计。

需求优先级规则：

1. 双端业务是否能闭环，以 `demo.html` 的房东与租客联动为最高优先级。
2. 租客页面字段和路由，以 `tenant-vue` 当前实现为联调基线。
3. 现有产品需求说明用于补足图文附件、租金区间、楼层、近地铁、财务报表、续租/转租等业务含义。
4. 原型硬编码值只作为演示种子数据，不作为长期业务规则。

---

## 1. 项目目标与范围

### 1.1 建设目标

建设一套同时服务租客端和房东后台的租房业务后端，使演示中的主要业务形成真实闭环：

1. 房东发布房源，租客可搜索、筛选、查看详情、收藏和对比。
2. 租客按房源发起私聊和预约，房东确认或拒绝预约。
3. 线下签约后，房东录入合同，房源自动转为已出租，租客可查看合同。
4. 房东生成账单，租客线下付款后报备，房东核销后账单完成。
5. 租客提交报修，房东受理、派单和完成维修，租客确认后闭环。
6. 租客提交退租，房东验房、结算押金，合同终止，房源重新可出租。
7. 租客评价，房东回复；社区、通知、黑名单和门店资料支撑运营与风控。

### 1.2 本期范围

- 租客身份认证、个人资料和退出登录。
- 租客身份资料与认证材料的采集、审核状态记录；不调用第三方实名核验。
- 房源列表、特价房源、组合筛选、排序、详情和浏览足迹。
- 收藏、最多三套房源对比。
- 按房源隔离的会话、文本消息、未读数和系统通知。
- 预约看房及房东侧处理。
- 合同/租约归档、查询、到期提醒和退租。
- 账单生成、付款报备、核销和状态查询。
- 报修提交、处理、完工、租客确认和状态轨迹。
- 评价、回复、社群配置、租客黑名单。
- 房东仪表盘、房源管理、租客管理、财务、维修、运营和门店设置所需接口。
- 房东主体认证资料、收款账户展示信息和通知偏好设置。
- 水电读数试算、账单图片、收入/支出统计和招租海报生成。
- 文件元数据管理，支持房源媒体、凭证、合同和报修图片。
- 审计日志、幂等、缓存、限流和基础可观测性。

### 1.3 非本期范围

- 在线支付、自动退款和支付渠道对账。
- 电子合同签章、公安/第三方自动实名核验和人脸识别；本期只采集资料并记录人工审核状态。
- 地图找房、智能门锁、在线水电表、第三方征信。
- 复杂多门店 SaaS 计费、财务总账和税务开票。
- 视频转码、内容审核平台和即时音视频通话。

线下支付、线下签约均保留线上状态记录和凭证，以保证业务可演示、可追踪。

---

## 2. 角色、权限与数据边界

| 角色 | 代码 | 核心权限 |
|---|---|---|
| 租客 | `TENANT` | 浏览房源、收藏/足迹/对比、聊天、预约、查看本人合同账单、报修、退租、评价 |
| 房东/门店员工 | `LANDLORD` | 管理所属门店房源、会话、预约、合同、账单、维修、评价和配置 |
| 平台管理员 | `ADMIN` | 跨门店查询、账号与门店管理、内容治理、审计查询 |

权限规则：

- 租客只能访问自己的会话、预约、合同、账单、报修和通知。
- 房东只能访问自己所属门店的数据；所有查询必须强制带服务端解析出的 `store_id`，不能信任前端传入值。
- 房源公开详情只返回可公开字段，不返回房东身份证、完整银行卡等敏感信息。
- 黑名单按“门店 + 租客”生效。被拉黑后仍可查看历史合同和账单，但不能新建会话、预约或新合同。
- 管理员的跨门店操作必须记录审计日志。
- 房东账号后续如需区分店长/客服/财务，可在 `LANDLORD` 下追加 RBAC 权限点；本期先保留扩展结构。

---

## 3. 页面功能与后端能力映射

### 3.1 租客端

| 页面/路由 | 页面功能 | 后端能力 |
|---|---|---|
| 首页 `/home` | 门店名称/地址、社群邀请和人数、特价横滑、位置/户型/特色筛选、价格/热度/最新排序、可租总数、房源列表、无结果 | 租客首页聚合、门店公开配置、特价列表、房源分页检索、筛选字典、曝光统计 |
| 房源详情 `/house/:houseId` | 图片/视频、当前价/原价、浏览数、发布时间、出租类型、户型面积、押付/最短租期、标签、设施矩阵、三段描述、评分、收藏、私聊、电话、预约 | 房源详情、浏览/联系计数、收藏状态、评价摘要、房东公开联系方式、会话创建、预约创建 |
| 心愿单 `/wishlist?tab=favorites\|history` | 收藏/足迹切换、空状态、最多选三套、两套起对比、对比中移除和进入详情 | 收藏增删查、足迹查清、批量房源对比；对比选择本身可保留前端会话状态 |
| 消息 `/messages` | 系统通知、按房源隔离的会话、最后一条消息、未读提示 | 通知列表、会话列表、未读数、最后消息摘要 |
| 私聊 `/messages/:houseId` | 历史消息、发送文本、预约卡片、预约状态、黑名单阻断 | 会话建/查、消息分页、消息发送/已读、预约关联、WebSocket 推送 |
| 我的 `/profile` | 微信授权资料、账单、图文报修及进度、合同/PDF、退租、评价、收藏、足迹、偏好推荐、退出 | 个人中心聚合、身份资料、账单流、维修流、租约流、评价流、推荐列表、会话注销 |

### 3.2 房东后台

| 模块 | 页面功能 | 后端能力 |
|---|---|---|
| 仪表盘 | 总房源、本月已收/待收、满租预计收益、空置率、15 天到期预警、今日待办、30 天转化漏斗、最近动态 | 统计口径、待办聚合、到期预警、漏斗日汇总、业务动态投影 |
| 房源管理 | 名称/状态搜索、新增、编辑、上下架、媒体、特价、招租海报、出租状态 | 房源 CRUD、发布校验、媒体排序、特价配置、海报生成、发布/占用双状态 |
| 线索与消息 | 按“房源+租客”会话、快捷回复、预约处理、从已确认预约生成合同 | 会话/消息、租客摘要、预约确认/拒绝、合同预填/一键激活 |
| 租客与合同 | 在租租客、合同详情、到期预警、退租、黑名单 | 租约、附件、退租结算、黑名单 |
| 财务 | 水电读数试算、账单图、费用明细、推送、付款凭证、核销/驳回、催缴、收入/支出/净利润 | 水电试算、账单及附件、付款报备、核销事务、通知、财务汇总和支出记录 |
| 维修 | 工单列表、受理、派单、处理、完工、关闭 | 工单状态机、处理人、附件、状态日志 |
| 运营 | 社群卡片、评价、回复、推荐/特价 | 社群配置、评价治理、房源运营字段 |
| 门店设置 | 主体/产权认证、收款账户展示、三类服务通知开关、店铺名称/地址/电话/介绍 | 门店资料、认证档案、收款账户、通知偏好、公开联系方式、文件元数据 |

### 3.3 页面级必需数据契约

以下字段是页面一比一联调的最低集合，后端 DTO 不得要求前端用常量补齐：

| 页面 DTO | 必需内容 |
|---|---|
| `TenantHomeDTO` | `store`、`community`、`specialListings`、`filterOptions`、首屏 `listingPage`、`availableCount` |
| `ListingCardDTO` | `listingId`、标题、1 主 2 副媒体预览、出租方式、户型名、面积、展示价、原价、优惠额、特价截止、描述摘要、标签、设施、收藏状态、可租状态 |
| `ListingDetailDTO` | 卡片全部字段、完整媒体、浏览数、发布时间、费用明细、押付、最短租期、可入住日、性别/宠物要求、设施矩阵、三段描述、门店名片、电话、评分摘要 |
| `ConversationSummaryDTO` | `conversationId`、房源摘要、对端用户摘要、最后消息类型/摘要/时间、未读数、会话状态 |
| `ChatMessageDTO` | 消息 ID、客户端消息 ID、发送方、类型、正文、业务卡片快照、业务对象 ID、发送/已读时间 |
| `AppointmentCardDTO` | 预约 ID、房源快照、日期时段、联系人、状态、拒绝/取消原因、房东回复 |
| `TenantProfileOverviewDTO` | 微信授权状态、头像/昵称/性别、收藏/足迹数量、有效租约摘要、待支付账单数、进行中工单数、可评价租约、推荐房源 |
| `LandlordDashboardDTO` | 4 个经营指标、到期预警、分类待办、漏斗、最近动态及各模块未处理数量 |

推荐房源首期规则与 Vue 页面保持一致：登录租客优先返回仍可租的收藏和最近足迹，去重后不足 6 套再按特价、热度、发布时间补齐；未登录用户直接按特价、热度和发布时间返回。已出租、下架和待签房源不能进入推荐结果。

### 3.4 双端闭环事件矩阵

| 租客/房东动作 | 同事务写入 | 事务后联动 | 另一端可见结果 |
|---|---|---|---|
| 房东发布/设特价 | 房源、媒体、特价版本 | 清房源缓存、记录动态 | 首页列表/特价区更新 |
| 租客发消息 | 消息、会话最后消息、未读数 | WebSocket、房东提醒 | 房东咨询列表实时出现 |
| 租客预约 | 预约、预约卡片消息 | 房东通知、仪表盘待办 | 房东可确认/拒绝 |
| 房东处理预约 | 预约状态、系统消息 | 租客通知、卡片推送 | 租客看到已确认/已拒绝 |
| 房东从预约生成合同 | 租约、房屋占用、房源下架、预约转化 | 合同通知、列表缓存失效 | 租客可查看合同，找房页不再展示 |
| 房东推账单 | 账单、明细、账单附件 | 租客通知/会话提醒 | 租客看到待支付账单与图片 |
| 租客报备付款 | 付款报备、账单待核销 | 房东财务待办 | 房东可核销/驳回 |
| 房东核销 | 报备与账单状态、审计 | 租客通知、经营指标更新 | 租客看到已支付 |
| 租客报修 | 工单、附件、初始日志 | 房东通知、维修待办 | 房东可受理/派单 |
| 房东标记完工 | 工单、完工附件、状态日志 | 租客通知 | 租客确认或退回处理 |
| 租客申请退租 | 退租申请、租约状态 | 房东待办 | 房东登记逐项验房/扣款 |
| 房东完成退租 | 结算、租约终止、房屋空置 | 退款记录通知、房源缓存失效 | 租客看到结清，房源按配置重新上架 |
| 租客评价/房东回复 | 评价或回复 | 评分重算、双方通知 | 详情与运营页同步 |

### 3.5 Vue 租客端接入时必须替换的 Mock 行为

`tenant-vue` 当前用于视觉和交互演示，以下项目不能原样带入真实联调：

| 当前 Mock 位置 | 现状 | 接入后要求 |
|---|---|---|
| `data.ts` 与 `rental.ts` | 房源、账单、工单、聊天直接在内存修改 | 改为 service 层调用第 7 章 API；Pinia 只保存页面状态和服务端返回数据 |
| `HouseView.vue` | 浏览数 97、发布时间 07-21、评分 8.8、费用与租期为硬编码 | 只使用 `ListingDetailDTO` 返回值，无数据时显示 `--` |
| `ProfileView.vue` | 合同、个人资料、退出登录和退租仅 Toast | 使用个人中心、租约、登出、退租接口，并刷新相关摘要 |
| `ChatView.vue` | 路由参数直接当聊天数据键，预约日期固定 | 先解析会话 ID；可约日期/时段来自预约时段接口；完整显示确认/拒绝/取消状态 |
| 账单/报修弹窗 | 未上传图片、账单只显示文本 | 使用文件上传返回的 `fileId`，账单详情展示私有签名 URL，报修展示处理时间线和前后附件 |
| 首页/详情卡片 | 店铺、社群、特价天数、收藏状态为本地值 | 使用 `TenantHomeDTO`/`ListingCardDTO`；特价倒计时根据 `specialEndAt` 客户端展示 |
| `House.id: number` | Mock 使用数字 ID | API DTO 使用字符串 ID；前端类型统一改为 `string`，避免 Snowflake 精度丢失 |

前端不应自行推导账单金额、合同有效状态、房屋是否可租、预约确认结果或维修最终状态；这些均以服务端响应为准。为支持演示阶段，保留 `VITE_API_MODE=mock`，但 Mock DTO 字段必须与 OpenAPI 保持同构。

---

## 4. 核心业务规则与状态机

### 4.1 房源发布状态与房屋占用状态

```text
房源发布状态 house_listing.publish_status：
DRAFT -> PUBLISHED <-> OFFLINE
PUBLISHED -> EXPIRED

物理房屋占用状态 property_unit.occupancy_status：
VACANT -> PENDING_SIGN -> OCCUPIED -> CHECKOUT_PENDING -> VACANT
VACANT/OCCUPIED -> MAINTENANCE -> 原合法状态
```

规则：

- 上架前必须具有标题、租金、地址/小区、户型、面积、至少一张图片和有效房东联系方式。
- 租客端可见条件固定为：`publish_status=PUBLISHED`、`occupancy_status=VACANT`、特价/发布时间有效且未软删除。不得只判断其中一个状态。
- 同一物理房屋同一时段只允许一个有效租约。
- 同一物理房屋同一时刻只允许一个 `PUBLISHED` 的当前招租房源；创建、上架、激活合同和退租完成时均需锁定 `property_unit` 校验，避免历史房源被误重新展示。
- 从预约生成合同草稿时可进入 `PENDING_SIGN` 并设置最多 48 小时的占用保留；草稿取消/超时恢复 `VACANT`。
- 激活租约时占用状态变为 `OCCUPIED`，关联房源自动 `OFFLINE`，下架原因为 `RENTED`；完成退租结算后占用状态恢复 `VACANT`，并根据 `autoRepublishAfterCheckout` 决定自动上架或保持下架。
- “即将到期”是活跃租约结束日期的派生标签，不写入房源发布状态；“待签”对应 `PENDING_SIGN`。
- 特价只对 `PUBLISHED` 生效，必须有特价金额和有效时间；特价金额必须小于标准租金。

### 4.2 预约状态

```text
PENDING -> CONFIRMED -> COMPLETED -> CONVERTED
PENDING -> DECLINED
PENDING/CONFIRMED -> CANCELLED
```

- 租客不能预约已出租、下架或被删除的房源。
- 同一租客对同一房源、同一时间段不可重复预约。
- 强制先建立该房源会话再创建预约；`conversationId` 必填且必须属于当前租客和该房源。
- 房东只能处理所属房源预约；确认、拒绝和取消均生成会话卡片及通知。
- 从预约生成并激活合同后预约进入 `CONVERTED`，保存 `tenancy_id`，不能再次生成合同。

### 4.3 账单状态

```text
DRAFT -> PENDING_PAYMENT -> PENDING_VERIFICATION -> PAID
                         -> PENDING_PAYMENT（房东驳回付款报备）
DRAFT/PENDING_PAYMENT -> CANCELLED
PENDING_PAYMENT -> OVERDUE（到期定时任务）
OVERDUE -> PENDING_VERIFICATION -> PAID
```

- 金额统一使用整数分，数据库字段为 `BIGINT`，不使用浮点数。
- 账单租金取激活租约的租金快照，不取房源当前标价或后来修改的特价。
- 水电试算由服务端根据上期读数、本期读数和租约/门店单价计算；本期读数不得小于上期读数。
- 租客“已付款”只代表报备，不能直接进入 `PAID`。
- 房东核销时锁定账单并校验当前状态，重复请求必须幂等。
- 已核销账单不可直接修改；冲正需后续单独建设，本期由管理员审计处理。

### 4.4 报修状态

```text
PENDING -> PROCESSING -> AWAITING_CONFIRMATION -> COMPLETED
PENDING/PROCESSING -> CANCELLED
AWAITING_CONFIRMATION -> PROCESSING（租客拒绝确认并补充说明）
```

- 报修必须关联有效租约；紧急情况可在租约结束后保留查看权限。
- 房东标记完工后进入 `AWAITING_CONFIRMATION`，租客确认后才算完成。
- 每次状态变化写入不可覆盖的状态日志。

### 4.5 租约与退租状态

```text
DRAFT -> ACTIVE -> EXPIRING -> EXPIRED
ACTIVE/EXPIRING -> CHECKOUT_PENDING -> TERMINATED
CHECKOUT_PENDING -> ACTIVE（退租申请驳回/撤销）
```

- 合同由房东录入，租客确认可见；本期不代表电子签章。
- 激活合同、房屋转 `OCCUPIED`、房源自动下架必须在同一数据库事务中完成；合同通知和缓存失效走提交后事件。
- 退租结算记录押金、扣款、退款和验房说明；完成后合同终止、房源恢复可租。
- 原型中的“双方签字确认”本期记录确认人、确认时间和线下确认备注，不等同于合规电子签名。

### 4.6 评价

- 只有存在有效或已结束租约的租客可以评价对应房源。
- 同一租约默认仅允许一条主评价，可在限定时间内修改。
- 评分范围 1–5；房东可回复，不可修改租客原文。
- 被隐藏的评价不参与公开评分聚合，但必须保留审计数据。

---

## 5. 总体架构设计

### 5.1 架构选择

首期采用“模块化单体 + 清晰领域边界”，避免在演示阶段引入微服务运维成本，同时保证未来可按模块拆分。

```text
租客 Vue 3 / 房东 Web
        |
 HTTPS REST + WebSocket
        |
Spring Boot 3.x（JDK 17）
  ├─ API 层：Controller、DTO、参数校验、统一异常
  ├─ 应用层：用例编排、权限、事务、幂等
  ├─ 领域层：实体、状态机、领域规则、领域事件
  ├─ 基础设施层：MySQL、Redis、文件存储、消息推送
  └─ 定时任务：过期、提醒、计数回写、数据清理
        |
 MySQL 8.0（事实数据） + Redis 7.x（缓存/会话/限流）
```

### 5.2 建议模块

| 模块 | 职责 |
|---|---|
| `auth` | 登录、令牌、登出、设备会话、权限 |
| `user` | 租客、房东资料、门店、黑名单 |
| `listing` | 物理房屋、房源、媒体、设施、筛选、收藏、足迹 |
| `conversation` | 会话、消息、未读数、WebSocket |
| `appointment` | 看房预约和状态流转 |
| `tenancy` | 合同/租约、附件、退租和押金结算 |
| `billing` | 账单、费用明细、付款报备和核销 |
| `repair` | 报修、附件、派单和状态日志 |
| `review` | 评价、回复、评分汇总 |
| `operation` | 社群配置、通知、通知偏好和经营动态 |
| `reporting` | 仪表盘、转化漏斗、收入/支出/净利润和每日汇总 |
| `file` | 文件上传策略、元数据、访问鉴权 |
| `audit` | 管理操作和关键状态变更审计 |

建议包结构：

```text
com.renthouse
  ├─ common        // 响应、异常、鉴权上下文、基础类型
  ├─ auth
  ├─ user
  ├─ listing
  ├─ conversation
  ├─ appointment
  ├─ tenancy
  ├─ billing
  ├─ repair
  ├─ review
  ├─ operation
  ├─ reporting
  ├─ file
  └─ audit
```

每个业务模块内部按 `api/application/domain/infrastructure` 分层，禁止 Controller 直接访问 Mapper，也禁止跨模块直接修改其他模块的数据表。

### 5.3 技术组件建议

- Spring Web、Validation、Security、WebSocket。
- MyBatis-Plus 或 Spring Data JPA 二选一；本项目复杂列表查询较多，建议 MyBatis-Plus + 明确 SQL。
- Flyway 管理数据库版本，禁止人工修改正式环境表结构。
- Redisson 用于分布式锁、限流和原子计数。
- springdoc-openapi 生成接口文档，接口 DTO 必须写字段说明和示例。
- MapStruct 负责实体与 DTO 转换，避免在 Controller 手工拼接。
- Micrometer + Actuator 暴露健康、指标；日志使用结构化 JSON。
- 文件存储通过 `FileStorageService` 抽象：开发环境可用本地目录，演示/生产使用 MinIO、OSS 或 S3 兼容存储；MySQL 只存元数据，不存二进制文件。

### 5.4 数据一致性策略

- MySQL 是业务事实唯一来源，Redis 不保存不可恢复的合同、账单和维修状态。
- 单模块内使用本地事务；激活合同、核销账单等关键操作使用行锁/乐观锁防并发。
- 跨模块副作用采用事务提交后事件，例如房东确认预约后再生成消息和通知。
- 对必须可靠执行的跨模块事件，建议使用本地事件表（Outbox）和重试任务；首期不强制引入 MQ。
- 所有状态变更接口必须验证“允许的前置状态”，不能仅依赖前端按钮是否显示。
- Outbox 消费必须以 `event_id` 幂等；通知、系统消息、经营动态和日报汇总任一失败都不能回滚已经完成的合同/账单主事务。

### 5.5 关键事务边界

| 用例 | 必须处于同一事务的数据 | 事务后异步动作 |
|---|---|---|
| 创建会话/发送消息 | 会话、消息、最后消息、数据库未读数 | WebSocket 推送、通知偏好判断 |
| 创建预约 | 预约、预约卡片消息、会话摘要 | 房东提醒、经营动态、漏斗计数 |
| 预约转合同并激活 | 租约、预约转化、房屋占用、房源自动下架 | 租客通知、缓存失效、日报计数 |
| 发布账单 | 账单、明细、账单附件、状态日志 | 租客通知、会话系统消息 |
| 核销账单 | 付款报备、账单、核销人/时间、审计 | 双端通知、财务汇总刷新 |
| 完成退租 | 验房明细、押金结算、租约终止、房屋空置、房源再发布决策 | 通知、缓存失效、经营动态 |

---

## 6. 接口通用规范

### 6.1 协议与格式

- REST API：`https://{host}/api/v1/**`。
- WebSocket：`wss://{host}/ws`。
- 字符集：UTF-8；时间：ISO 8601，服务端数据库统一保存 UTC，界面按 Asia/Shanghai 展示。
- 金额：请求和响应统一使用 `xxxAmountCents` 整数分。
- ID：JSON 中使用字符串，避免 JavaScript 大整数精度丢失；数据库使用 `BIGINT UNSIGNED`。
- 枚举：使用稳定英文代码，中文文案由前端映射或响应中的字典提供。
- 客户端使用 `X-Client-Type: TENANT_WEB|WECHAT_MINIPROGRAM|LANDLORD_WEB` 标识渠道，用于登录策略、审计和行为统计，不能用它代替角色鉴权。

### 6.2 统一响应

```json
{
  "code": "OK",
  "message": "成功",
  "data": {},
  "requestId": "01J...",
  "timestamp": "2026-07-31T10:20:30+08:00"
}
```

分页数据：

```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "total": 86,
  "hasNext": true
}
```

### 6.3 请求规范

- 分页默认 `page=1&size=20`，最大 `size=100`。
- 列表排序仅接受白名单字段，禁止直接拼接客户端传入列名。
- 创建、付款报备、核销、状态流转接口支持请求头 `Idempotency-Key`。
- 修改资源需携带 `version`，服务端冲突返回 `409`。
- 受保护接口使用 `Authorization: Bearer <accessToken>`。

### 6.4 HTTP 状态与业务错误码

| HTTP | 业务码示例 | 场景 |
|---|---|---|
| 400 | `VALIDATION_ERROR` | 参数缺失、格式错误 |
| 401 | `AUTH_REQUIRED`、`TOKEN_EXPIRED` | 未登录或令牌过期 |
| 403 | `ACCESS_DENIED`、`TENANT_BLOCKED` | 无权限或被拉黑 |
| 404 | `LISTING_NOT_FOUND` | 资源不存在或不可见 |
| 409 | `STATE_CONFLICT`、`VERSION_CONFLICT` | 状态或并发版本冲突 |
| 422 | `APPOINTMENT_TIME_CONFLICT` | 业务参数有效但规则不允许 |
| 429 | `RATE_LIMITED` | 请求过频 |
| 500 | `INTERNAL_ERROR` | 未预期异常，响应不暴露堆栈 |

补充业务码：`COMPARE_LIMIT_EXCEEDED`、`LISTING_NOT_AVAILABLE`、`BILL_ALREADY_VERIFIED`、`REPAIR_INVALID_TRANSITION`、`ACTIVE_TENANCY_EXISTS`、`DUPLICATE_REVIEW`、`FILE_TYPE_NOT_ALLOWED`。

---

## 7. 接口设计

以下为开发基线接口。查询接口默认只返回调用者有权访问的数据。

### 7.1 认证、用户与门店

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/auth/wechat/login` | 公开 | 微信 `code` 换取用户并签发令牌 |
| POST | `/auth/password/login` | 公开 | 房东/管理员账号密码登录 |
| POST | `/auth/demo-login` | 仅 local/staging | 固定演示账号登录，生产环境必须禁用 |
| POST | `/auth/refresh` | 持有刷新令牌 | 刷新访问令牌并轮换刷新令牌 |
| POST | `/auth/logout` | 登录 | 注销当前设备会话 |
| GET | `/me` | 登录 | 当前用户和角色资料 |
| PATCH | `/me` | 登录 | 更新昵称、头像、手机号等可编辑资料 |
| GET | `/me/profile-overview` | 租客 | 个人中心聚合数据、数量和推荐 |
| GET | `/me/verification` | 租客 | 身份资料及人工审核状态 |
| PUT | `/me/verification` | 租客 | 保存实名资料并提交审核 |
| POST | `/me/verification/documents` | 租客 | 关联已上传身份材料 |
| GET | `/me/notification-preferences` | 登录 | 当前通知开关 |
| PUT | `/me/notification-preferences` | 登录 | 更新咨询、预约合同、账单维修提醒开关 |
| GET | `/stores/{storeId}/public-profile` | 公开 | 门店名称、介绍、电话、资质公开项 |
| GET | `/landlord/store` | 房东 | 当前门店完整资料 |
| PUT | `/landlord/store` | 房东 | 更新门店资料和公开联系方式 |
| GET | `/landlord/store/verification` | 房东 | 主体、身份、产权认证摘要和材料 |
| PUT | `/landlord/store/verification` | 房东 | 保存/提交认证资料 |
| GET | `/landlord/store/payment-account` | 房东 | 收款方式及脱敏账号 |
| PUT | `/landlord/store/payment-account` | 房东 | 更新线下收款账户展示信息 |

微信登录请求：

```json
{
  "code": "wx-login-code",
  "nickname": "小橙",
  "avatarUrl": "https://...",
  "phoneCredential": null
}
```

登录响应 `data` 至少包含：`accessToken`、`expiresIn`、`refreshToken`、`user`、`roles`、`needsProfileCompletion`。

### 7.2 房源公开查询与互动

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/tenant/home` | 公开/登录 | 首页首屏聚合：门店、社群、特价、筛选字典和首屏房源 |
| GET | `/listings` | 公开 | 房源分页、筛选、搜索和排序 |
| GET | `/listings/specials` | 公开 | 当前有效特价房源 |
| GET | `/listings/recommended` | 公开/登录 | 推荐房源；登录用户优先收藏/足迹，未登录按特价、热度和发布时间 |
| GET | `/listings/{listingId}` | 公开 | 房源详情、设施、媒体、评分摘要 |
| POST | `/listings/{listingId}/views` | 可匿名 | 记录浏览；匿名使用设备标识去重 |
| POST | `/listings/{listingId}/contact-clicks` | 可匿名 | 记录聊天/电话/预约入口点击，用于漏斗 |
| GET | `/listings/{listingId}/appointment-slots` | 公开 | 返回指定日期可预约时段 |
| GET | `/listing-filters` | 公开 | 位置、户型、设施、租赁条件字典 |
| PUT | `/me/favorites/{listingId}` | 租客 | 收藏，重复调用保持成功 |
| DELETE | `/me/favorites/{listingId}` | 租客 | 取消收藏 |
| GET | `/me/favorites` | 租客 | 收藏分页列表 |
| GET | `/me/browse-history` | 租客 | 足迹按最后浏览时间倒序 |
| DELETE | `/me/browse-history/{listingId}` | 租客 | 删除一条足迹 |
| DELETE | `/me/browse-history` | 租客 | 清空足迹 |
| GET | `/listings/compare?ids=1,2,3` | 公开 | 返回最多三套房源的对比字段 |
| POST | `/analytics/events/batch` | 可匿名 | 批量上报房源曝光等轻量行为，服务端校验白名单 |

`GET /listings` 查询参数：

| 参数 | 类型 | 说明 |
|---|---|---|
| `storeId` | string | 门店；单门店演示可由配置默认 |
| `keyword` | string | 标题、小区、商圈或地址关键词 |
| `districtCode`、`areaCode`、`communityName` | string | 区域/原型中的村名位置筛选 |
| `layoutCodes` | string[] | 单间、大单间、一/二/三/四房一厅、其他，可多选 |
| `featureCodes` | string[] | 设施/特色代码，多选默认 AND 语义 |
| `rentalType` | string | 整租/合租 |
| `minRentCents`、`maxRentCents` | long | 月租区间 |
| `floorRanges`、`nearMetro` | string[]/boolean | 产品需求预留的楼层和近地铁筛选 |
| `specialOnly` | boolean | 只看特价 |
| `sort` | string | `DEFAULT`、`RENT_ASC`、`RENT_DESC`、`NEWEST`、`POPULAR` |
| `page`、`size` | int | 分页 |

`POST /listings/{listingId}/contact-clicks` 请求体为 `{ "action": "CHAT|CALL|APPOINTMENT" }`；`GET /listings/{listingId}/appointment-slots` 必须带 `date=YYYY-MM-DD`。`POST /analytics/events/batch` 仅允许 `LISTING_IMPRESSION`、`DETAIL_VIEW`、`COMMUNITY_CLICK` 三种事件，并同时按设备/IP/用户限流，不能接受任意事件名。

房源列表项至少包含：ID、标题、封面、媒体类型、有效展示租金、原租金、特价标识、小区/区域、户型、面积、标签、收藏状态、浏览数和发布时间。

房源详情额外包含：媒体数组、楼层、租赁方式、押付方式、最短租期、可入住日、费用说明、设施矩阵、描述分段、房东公开电话、门店信息、5 分制原始平均分、页面使用的 10 分制展示分和评价数。`isFavorited` 仅登录后返回真实值；`displayRating10 = round(averageRating5 * 2, 1)`，没有评价时返回 `null` 而不是伪造 8.8。

`/tenant/home` 只负责首屏减少请求；切换筛选和加载更多仍调用 `/listings`。聚合接口内部必须复用相同查询用例，禁止复制两套筛选逻辑。

### 7.3 房东房源管理

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/landlord/listings` | 按状态、关键词、特价筛选所属房源 |
| POST | `/landlord/listings` | 新建物理房屋及房源草稿 |
| GET | `/landlord/listings/{id}` | 编辑详情 |
| PUT | `/landlord/listings/{id}` | 完整更新草稿/下架房源 |
| POST | `/landlord/listings/{id}/media` | 关联已上传媒体 |
| PUT | `/landlord/listings/{id}/media-order` | 调整媒体顺序和封面 |
| DELETE | `/landlord/listings/{id}/media/{mediaId}` | 移除媒体引用 |
| POST | `/landlord/listings/{id}/publish` | 校验并上架 |
| POST | `/landlord/listings/{id}/offline` | 下架，需填写原因 |
| PUT | `/landlord/listings/{id}/special` | 设置/取消特价及有效期 |
| POST | `/landlord/listings/{id}/poster` | 生成招租海报，返回公开图片 `fileId` 和 URL |
| DELETE | `/landlord/listings/{id}` | 仅草稿可软删除 |

创建/更新房源 DTO 核心字段：`property`（地址、房号、楼层）、`title`、`rentalType`、`layout`、`areaSqm`、`rentAmountCents`、各项费用、`depositMonths`、`paymentCycleMonths`、`minimumLeaseMonths`、`availableDate`、`descriptionSections`、`featureCodes`、`version`。

海报内容取房源当前封面、标题、展示价、户型面积、门店名和小程序码/访问码。生成海报不修改房源状态；同一房源同一版本可复用已有结果，房源关键字段变更后缓存失效。

### 7.4 会话与消息

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/conversations` | 租客 | 按房源创建或返回现有会话 |
| GET | `/conversations` | 租客/房东 | 会话列表、最后消息、未读数 |
| GET | `/conversations/{id}` | 会话成员 | 会话和关联房源摘要 |
| GET | `/conversations/{id}/messages` | 会话成员 | 游标分页历史消息 |
| POST | `/conversations/{id}/messages` | 会话成员 | REST 发送消息，WebSocket 不可用时兜底 |
| POST | `/conversations/{id}/read` | 会话成员 | 标记已读至指定消息 |
| POST | `/conversations/{id}/close` | 房东 | 关闭会话；历史保留 |

创建会话：

```json
{
  "listingId": "10001"
}
```

发送文本：

```json
{
  "clientMessageId": "b4e5b6d6-...",
  "type": "TEXT",
  "content": "您好，周六下午可以看房吗？"
}
```

- `clientMessageId` 在“发送者 + 会话”内唯一，用于断线重发去重。
- 消息类型预留 `TEXT`、`IMAGE`、`APPOINTMENT_CARD`、`SYSTEM`。
- 建议 WebSocket 客户端向 `/app/chat.send` 发送，订阅 `/user/queue/messages`、`/user/queue/notifications`；服务端仍持久化到 MySQL 后再确认成功。
- 历史消息使用 `beforeMessageId` + `size` 游标分页，避免翻页期间漏消息。
- 会话唯一语义是“门店 + 房源 + 租客 + 接待房东”，房东页面可以按房源分组展示，但绝不能把同一房源的多个租客消息合并。
- Vue 的 `/messages/:houseId` 路由首次进入时先以 `houseId/listingId` 调用 `POST /conversations` 获取当前租客的 `conversationId`，后续消息读写只使用 `conversationId`；不得按房源 ID 直接读取任意会话。
- 原型中的三个快捷回复文案由房东 Web 在输入框中填充，最终仍走同一个发送消息接口；首期无需单独后端接口。
- 拉黑后阻断租客发送和新预约，但允许房东发送履约、账单和安全相关消息；系统消息始终可达。

### 7.5 预约看房

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/appointments` | 租客 | 新建预约 |
| GET | `/me/appointments` | 租客 | 我的预约 |
| POST | `/appointments/{id}/cancel` | 租客 | 取消待处理/已确认预约 |
| GET | `/landlord/appointments` | 房东 | 按日期、状态和房源查询 |
| POST | `/landlord/appointments/{id}/confirm` | 房东 | 确认，可调整时间 |
| POST | `/landlord/appointments/{id}/decline` | 房东 | 拒绝并填写原因 |
| POST | `/landlord/appointments/{id}/complete` | 房东 | 标记已带看 |
| POST | `/landlord/appointments/{id}/tenancy` | 房东 | 从已确认/已带看预约预填并生成合同，可选择立即激活 |

```json
{
  "listingId": "10001",
  "conversationId": "30001",
  "startAt": "2026-08-02T14:00:00+08:00",
  "endAt": "2026-08-02T14:30:00+08:00",
  "contactName": "小橙",
  "contactPhone": "138****0000",
  "note": "到楼下请联系我"
}
```

预约转合同请求至少包含：`startDate`、`endDate`、`rentAmountCents`、`depositAmountCents`、`paymentCycleMonths`、`attachmentFileIds`、`activateImmediately`、`draftHoldHours`。服务端从预约读取租客和房源，禁止客户端替换为其他租客/房源。重复转化返回已有租约或 `APPOINTMENT_ALREADY_CONVERTED`。

### 7.6 合同、租约和退租

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/me/tenancies` | 租客 | 本人租约列表 |
| GET | `/me/tenancies/{id}` | 租客 | 合同、租期、费用和附件 |
| POST | `/me/tenancies/{id}/checkout-requests` | 租客 | 发起退租 |
| GET | `/me/checkout-requests/{id}` | 租客 | 查看退租进度和结算 |
| GET | `/landlord/tenancies` | 房东 | 租客/租约列表 |
| POST | `/landlord/tenancies` | 房东 | 录入合同草稿 |
| GET | `/landlord/tenancies/{id}` | 房东 | 合同档案、租客资料和附件 |
| PUT | `/landlord/tenancies/{id}` | 房东 | 更新草稿 |
| POST | `/landlord/tenancies/{id}/activate` | 房东 | 激活合同并占用房源 |
| POST | `/landlord/tenancies/{id}/cancel-draft` | 房东 | 取消草稿并释放待签占用 |
| POST | `/landlord/tenancies/{id}/attachments` | 房东 | 关联合同附件 |
| GET | `/landlord/checkout-requests` | 房东 | 退租待办 |
| POST | `/landlord/checkout-requests/{id}/inspect` | 房东 | 登记验房结果和扣款 |
| POST | `/landlord/checkout-requests/{id}/reject` | 房东 | 驳回申请 |
| POST | `/landlord/checkout-requests/{id}/settle` | 房东 | 完成押金结算并终止租约 |

激活合同必须校验：租客未被拉黑、房源可租、日期区间有效、没有重叠有效租约、租金/押金非负、附件满足门店规则。

### 7.7 账单与付款核销

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/me/bills` | 租客 | 按租约、月份、状态查询 |
| GET | `/me/bills/{id}` | 租客 | 账单费用明细和状态轨迹 |
| POST | `/me/bills/{id}/payment-reports` | 租客 | 报备线下付款并上传凭证 |
| GET | `/landlord/bills` | 房东 | 财务账单查询 |
| GET | `/landlord/bills/{id}` | 房东 | 账单、附件、付款报备和轨迹 |
| POST | `/landlord/tenancies/{id}/bill-preview` | 房东 | 按上期/本期水电读数服务端试算 |
| POST | `/landlord/bills` | 房东 | 创建单张账单 |
| POST | `/landlord/bills/batch-generate` | 房东 | 按活跃租约批量生成月账单 |
| PUT | `/landlord/bills/{id}` | 房东 | 仅草稿/待支付可编辑 |
| POST | `/landlord/bills/{id}/issue` | 房东 | 发布给租客 |
| POST | `/landlord/bills/{id}/verify` | 房东 | 核销付款 |
| POST | `/landlord/bills/{id}/reject-payment` | 房东 | 驳回付款报备 |
| POST | `/landlord/bills/{id}/remind` | 房东 | 生成催缴通知，需限频 |
| POST | `/landlord/bills/{id}/cancel` | 房东 | 作废未核销账单 |
| GET | `/landlord/finance/summary` | 房东 | 按月/房源统计应收、已收、支出和净收益 |
| GET | `/landlord/expenses` | 房东 | 支出记录分页 |
| POST | `/landlord/expenses` | 房东 | 登记维修、保洁等支出和附件 |
| PUT | `/landlord/expenses/{id}` | 房东 | 修改未锁定支出 |
| DELETE | `/landlord/expenses/{id}` | 房东 | 软删除未锁定支出 |

付款报备：

```json
{
  "paidAmountCents": 326000,
  "paidAt": "2026-07-31T09:30:00+08:00",
  "paymentMethod": "BANK_TRANSFER",
  "paymentReference": "尾号 1688",
  "attachmentFileIds": ["90001"],
  "remark": "已转账，请核实",
  "version": 2
}
```

核销响应必须返回最新账单状态、核销人、核销时间和新 `version`。

账单创建请求除费用明细外必须支持 `billAttachmentFileIds`，用于电表、水表和房东生成的账单图片。试算接口返回上期读数、本期用量、单价、分项金额、合同租金快照和总额；创建接口必须在服务端重新计算，不能信任前端提交的总额。净收益口径固定为统计期内已核销收入减已登记有效支出，待支付和待核销不计入已收。

### 7.8 报修

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| POST | `/me/repair-tickets` | 租客 | 提交报修 |
| GET | `/me/repair-tickets` | 租客 | 我的报修列表 |
| GET | `/me/repair-tickets/{id}` | 租客 | 工单详情和时间线 |
| POST | `/me/repair-tickets/{id}/confirm` | 租客 | 确认完工 |
| POST | `/me/repair-tickets/{id}/reject-completion` | 租客 | 拒绝完工并退回处理中 |
| GET | `/landlord/repair-tickets` | 房东 | 工单查询 |
| POST | `/landlord/repair-tickets/{id}/accept` | 房东 | 受理 |
| POST | `/landlord/repair-tickets/{id}/assign` | 房东 | 指定维修人员和预约时间 |
| POST | `/landlord/repair-tickets/{id}/mark-repaired` | 房东 | 标记完工，等待租客确认 |
| POST | `/landlord/repair-tickets/{id}/cancel` | 房东 | 取消并记录原因 |

提交字段：`tenancyId`、`categoryCode`、`description`、`urgency`、`preferredVisitAt`、`contactPhone`、`attachmentFileIds`。

### 7.9 评价、社群与通知

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/listings/{id}/reviews` | 公开 | 可见评价分页 |
| POST | `/me/reviews` | 租客 | 对租约/房源提交评价 |
| PUT | `/me/reviews/{id}` | 租客 | 允许期内修改本人评价 |
| GET | `/landlord/reviews` | 房东 | 评价管理 |
| POST | `/landlord/reviews/{id}/reply` | 房东 | 回复评价 |
| POST | `/landlord/reviews/{id}/visibility` | 房东/管理员 | 隐藏需填写治理原因 |
| GET | `/stores/{storeId}/community` | 公开 | 首页社群邀请配置 |
| PUT | `/landlord/community` | 房东 | 更新社群文案、图片、入口和开关 |
| POST | `/stores/{storeId}/community/join-events` | 登录可选 | 记录进群点击/确认；只有可确认加入时才增加成员数 |
| GET | `/me/notifications` | 登录 | 系统通知分页 |
| GET | `/me/notifications/unread-count` | 登录 | TabBar 与列表未读数 |
| POST | `/me/notifications/{id}/read` | 登录 | 标记单条已读 |
| POST | `/me/notifications/read-all` | 登录 | 全部已读 |

### 7.10 黑名单、文件、仪表盘和审计

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/landlord/blacklist` | 房东 | 门店黑名单 |
| POST | `/landlord/blacklist` | 房东 | 拉黑租客并记录原因 |
| DELETE | `/landlord/blacklist/{id}` | 房东 | 解除黑名单 |
| POST | `/files/upload` | 登录 | 小文件直传；返回 `fileId` |
| POST | `/files/presign` | 登录 | 大文件获取对象存储直传凭证 |
| POST | `/files/{id}/complete` | 登录 | 确认直传完成并校验元数据 |
| GET | `/files/{id}/access-url` | 有权用户 | 获取短期签名访问地址 |
| GET | `/landlord/dashboard` | 房东 | 首页聚合指标和待办 |
| GET | `/admin/audit-logs` | 管理员 | 关键操作审计查询 |

仪表盘响应必须包含：

- `metrics`：房源总数、本月已收、本月待收、满租月预计收益、空置率及同比/环比可空字段。
- `expiryWarnings`：未来 15 天到期租约及租客/房源摘要。
- `todos`：待处理维修、退租、预约和待核销账单，含 `businessType/businessId/actionPath`。
- `funnel30d`：房源曝光、详情浏览、发起咨询、预约、签约数及逐级转化率。
- `recentActivities`：最近业务动态，不直接暴露审计日志中的敏感差异。
- `menuBadges`：咨询/预约、维修、财务等模块未处理数量。

统计口径：空置率 = `VACANT` 且可经营房屋数 / 非归档可经营房屋总数；本月已收按账单 `verified_at` 落入本月统计；本月待收包含 `PENDING_PAYMENT/OVERDUE/PENDING_VERIFICATION`；满租预计收益对已出租房屋取活跃租约租金快照，对空置房屋取当前有效房源展示价。

---

## 8. 数据库设计

### 8.1 通用约定

- 存储引擎 `InnoDB`，字符集 `utf8mb4`，排序规则项目统一。
- 主键 `id BIGINT UNSIGNED`，由应用雪花算法生成；对外以字符串传输。
- 业务表通用字段：`created_at DATETIME(3)`、`updated_at DATETIME(3)`、`created_by`、`updated_by`、`version INT`、必要时 `deleted_at DATETIME(3)`。
- 金额为 `BIGINT` 分；面积为 `DECIMAL(10,2)`；经纬度为 `DECIMAL(10,7)`。
- 状态字段使用 `VARCHAR(32)`，代码层枚举校验，不使用 MySQL `ENUM`，便于演进。
- 手机、身份证、银行卡等敏感字段应用层加密；另存脱敏展示字段或查询哈希。
- 软删除数据仍受必要的业务唯一性约束；可采用状态唯一键或归档表处理。

### 8.2 用户、认证与门店

#### `sys_user` 用户主表

| 字段 | 类型 | 约束/说明 |
|---|---|---|
| `id` | BIGINT UNSIGNED | PK |
| `user_no` | VARCHAR(32) | 唯一业务编号 |
| `nickname` | VARCHAR(64) | 昵称 |
| `avatar_url` | VARCHAR(512) | 头像 |
| `gender_code` | VARCHAR(16) | `UNKNOWN/MALE/FEMALE`，对应个人中心展示 |
| `phone_cipher` | VARCHAR(512) | 加密手机号 |
| `phone_hash` | CHAR(64) | 精确查询哈希，普通索引 |
| `status` | VARCHAR(32) | `ACTIVE/LOCKED/DISABLED` |
| `last_login_at` | DATETIME(3) | 最近登录 |
| 通用字段 | - | 含软删除 |

索引：`uk_user_no(user_no)`、`idx_phone_hash(phone_hash)`、`idx_status_created(status, created_at)`。

#### `user_auth_identity` 登录身份

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK -> `sys_user` |
| `identity_type` | VARCHAR(32) | `WECHAT_OPENID/PASSWORD/PHONE` |
| `identifier` | VARCHAR(191) | openid、账号或手机号哈希 |
| `credential_hash` | VARCHAR(255) | 密码哈希；微信身份为空 |
| `union_id` | VARCHAR(128) | 微信 UnionId，可空 |
| `verified_at` | DATETIME(3) | 验证时间 |
| `last_used_at` | DATETIME(3) | 最近使用 |

唯一索引：`uk_identity(identity_type, identifier)`；索引：`idx_auth_user(user_id)`。

#### `user_role` 用户角色

字段：`id`、`user_id`、`role_code`、`store_id`（全局租客/管理员固定为 `0`，门店角色为真实门店 ID）、通用字段。  
唯一索引：`uk_user_role(user_id, role_code, store_id)`。

#### `tenant_profile` 租客资料

字段：`user_id` PK/FK、`real_name_cipher`、`id_no_cipher`、`id_no_hash`、`emergency_contact_cipher`、`profile_completed`、`default_contact_phone_cipher`、通用字段。

#### `tenant_verification` 与 `tenant_verification_document`

`tenant_verification` 字段：`id`、`tenant_user_id`、`verification_status(DRAFT/SUBMITTED/APPROVED/REJECTED)`、`submitted_at`、`reviewed_by`、`reviewed_at`、`rejection_reason`、通用字段；唯一索引：`uk_tenant_verification(tenant_user_id)`。  
`tenant_verification_document` 字段：`id`、`verification_id`、`file_id`、`document_type(ID_FRONT/ID_BACK/OTHER)`、`created_at`；唯一索引：`uk_verification_document(verification_id, file_id)`。

#### `store` 门店/房东主体

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT | PK |
| `store_code` | VARCHAR(32) | 唯一编号 |
| `name` | VARCHAR(128) | 首页/后台展示名称 |
| `short_name` | VARCHAR(64) | 移动端短名称 |
| `description` | TEXT | 门店介绍 |
| `public_phone` | VARCHAR(32) | 公开咨询电话 |
| `address` | VARCHAR(255) | 门店地址 |
| `logo_file_id` | BIGINT | 文件引用 |
| `business_license_file_id` | BIGINT | 资质文件引用 |
| `status` | VARCHAR(32) | `ACTIVE/DISABLED` |
| 通用字段 | - | 含软删除 |

#### `store_member`

字段：`id`、`store_id`、`user_id`、`display_name`、`position`、`member_status`、`joined_at`、通用字段。  
唯一索引：`uk_store_member(store_id, user_id)`。

#### `store_verification` 与 `store_payment_account`

`store_verification` 字段：`id`、`store_id`、`verification_type(OWNER_ID/PROPERTY_RIGHT/BUSINESS_LICENSE)`、`status(DRAFT/SUBMITTED/APPROVED/REJECTED)`、`holder_name_cipher`、`certificate_no_cipher`、`certificate_no_hash`、`reviewed_by`、`reviewed_at`、`rejection_reason`、通用字段；索引：`idx_store_verification(store_id, status)`。  
`store_payment_account` 字段：`id`、`store_id`、`account_type(WECHAT/BANK/OTHER)`、`account_name_cipher`、`account_no_cipher`、`account_no_masked`、`status(ACTIVE/DISABLED)`、`verified_at`、通用字段；唯一索引：`uk_active_payment_account(store_id, account_type, status)`。本表只记录线下收款展示信息，不发起线上扣款。

#### `tenant_blacklist`

字段：`id`、`store_id`、`tenant_user_id`、`reason`、`evidence_file_id`、`status(BLOCKED/RELEASED)`、`blocked_by`、`blocked_at`、`released_by`、`released_at`、通用字段。  
索引：`idx_blacklist_check(store_id, tenant_user_id, status)`。

### 8.3 房屋、房源、媒体和互动

#### `property_unit` 物理房屋

字段：`id`、`store_id`、`property_code`、`community_name`、`district_code`、`area_code`、`address`、`building_no`、`unit_no`、`room_no`、`floor_no`、`total_floors`、`has_elevator`、`near_metro`、`longitude`、`latitude`、`occupancy_status(VACANT/PENDING_SIGN/OCCUPIED/CHECKOUT_PENDING/MAINTENANCE)`、`ownership_note`、通用字段。  
唯一索引：`uk_property_code(store_id, property_code)`；索引：`idx_property_area(store_id, district_code, area_code, occupancy_status)`。

#### `house_listing` 房源

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT | PK |
| `store_id`、`property_id` | BIGINT | 门店、物理房屋 |
| `listing_no` | VARCHAR(32) | 唯一房源编号 |
| `title` | VARCHAR(160) | 展示标题 |
| `rental_type` | VARCHAR(32) | `WHOLE/SHARED` |
| `layout_code` | VARCHAR(32) | 如 `1R1L1B` |
| `bedroom_count`、`living_room_count`、`bathroom_count` | TINYINT | 户型拆分字段 |
| `area_sqm` | DECIMAL(10,2) | 面积 |
| `rent_amount_cents` | BIGINT | 标准月租 |
| `management_fee_cents`、`network_fee_cents` | BIGINT | 固定费用 |
| `water_price_cents`、`electricity_price_cents` | BIGINT | 单价，按门店业务口径 |
| `deposit_months`、`payment_cycle_months` | TINYINT | 押付方式 |
| `minimum_lease_months` | SMALLINT | 最短租期 |
| `available_date` | DATE | 可入住日 |
| `description` | TEXT | 房源描述 |
| `living_experience`、`source_description`、`surrounding_description` | TEXT | 详情分段 |
| `publish_status` | VARCHAR(32) | `DRAFT/PUBLISHED/OFFLINE/EXPIRED`，不承载占用状态 |
| `is_special` | TINYINT(1) | 特价标记 |
| `special_amount_cents` | BIGINT | 特价，可空 |
| `special_start_at`、`special_end_at` | DATETIME(3) | 特价窗口 |
| `published_at`、`offline_at` | DATETIME(3) | 上下架时间 |
| `offline_reason` | VARCHAR(32) | `MANUAL/RENTED/EXPIRED/MAINTENANCE` |
| `auto_republish_after_checkout` | TINYINT(1) | 退租结算后是否自动上架 |
| `view_count` | BIGINT | 持久化浏览数 |
| `hot_score` | DECIMAL(16,4) | 推荐排序分 |
| 通用字段 | - | `version` 必须启用 |

索引建议：

- `uk_listing_no(listing_no)`。
- `idx_listing_public(store_id, publish_status, published_at, id)`。
- `idx_listing_rent(store_id, publish_status, rent_amount_cents, id)`。
- `idx_listing_area(store_id, publish_status, property_id)`；地区字段高频时可冗余到本表并建立组合索引。
- `idx_listing_special(store_id, publish_status, is_special, special_end_at)`。
- 标题/小区关键词首期可用受控 `LIKE`；数据量增大后接入 Elasticsearch/OpenSearch，不在首期强制范围。

#### `listing_media`

字段：`id`、`listing_id`、`file_id`、`media_type(IMAGE/VIDEO)`、`external_url`（迁移 demo 外链时使用）、`poster_file_id`、`duration_seconds`、`width`、`height`、`sort_order`、`is_cover`、`status`、通用字段。  
索引：`idx_media_listing(listing_id, status, sort_order)`；同一房源只能有一张有效封面，由应用事务保证。

#### `feature_dictionary`

字段：`id`、`feature_code`、`feature_name`、`category(AMENITY/CONDITION/TAG/LAYOUT)`、`filter_group(HIGHLIGHT/AMENITY/LEASE_TERM/FLOOR/OTHER)`、`icon`、`sort_order`、`enabled`、通用字段。  
唯一索引：`uk_feature_code(feature_code)`。

#### `listing_feature`

字段：`id`、`listing_id`、`feature_id`、`feature_value`、通用字段。  
唯一索引：`uk_listing_feature(listing_id, feature_id)`；反向索引：`idx_feature_listing(feature_id, listing_id)`，用于多条件筛选。

#### `tenant_favorite`

字段：`id`、`tenant_user_id`、`listing_id`、`created_at`。  
唯一索引：`uk_favorite(tenant_user_id, listing_id)`；索引：`idx_favorite_time(tenant_user_id, created_at, id)`。

#### `browse_history`

字段：`id`、`tenant_user_id`、`listing_id`、`first_viewed_at`、`last_viewed_at`、`view_times`。  
唯一索引：`uk_history(tenant_user_id, listing_id)`；索引：`idx_history_time(tenant_user_id, last_viewed_at, id)`。重复浏览更新最后时间和次数，不无限插入。

#### `listing_daily_metric`

字段：`id`、`metric_date`、`store_id`、`listing_id`、`impression_count`、`detail_view_count`、`contact_click_count`、`favorite_count`、`conversation_start_count`、`appointment_count`、`tenancy_conversion_count`、`updated_at`。  
唯一索引：`uk_listing_metric(metric_date, listing_id)`；索引：`idx_store_metric(store_id, metric_date)`。用于房源热度和 30 天漏斗，不保存可识别的匿名设备信息。

### 8.4 会话、消息、预约和通知

#### `conversation`

字段：`id`、`store_id`、`listing_id`、`tenant_user_id`、`landlord_user_id`、`status(NORMAL/BLOCKED/CLOSED)`、`last_message_id`、`last_message_at`、`tenant_unread_count`、`landlord_unread_count`、`tenant_last_read_message_id`、`landlord_last_read_message_id`、通用字段。  
唯一索引：`uk_conversation(listing_id, tenant_user_id, landlord_user_id)`；索引：`idx_tenant_chat(tenant_user_id, last_message_at)`、`idx_landlord_chat(store_id, landlord_user_id, last_message_at)`。

#### `chat_message`

字段：`id`、`conversation_id`、`sender_user_id`、`client_message_id`、`message_type`、`content`、`payload_json`（预约卡片等展示快照）、`business_type`、`business_id`、`sent_at`、`recalled_at`、通用字段。  
唯一索引：`uk_client_message(conversation_id, sender_user_id, client_message_id)`；索引：`idx_message_cursor(conversation_id, id)`。消息正文不做软删除，撤回使用状态字段。

#### `viewing_appointment`

字段：`id`、`appointment_no`、`store_id`、`listing_id`、`conversation_id`、`tenant_user_id`、`landlord_user_id`、`start_at`、`end_at`、`contact_name`、`contact_phone_cipher`、`tenant_note`、`landlord_note`、`status(PENDING/CONFIRMED/DECLINED/CANCELLED/COMPLETED/CONVERTED)`、`tenancy_id`、`confirmed_at`、`completed_at`、`converted_at`、`cancelled_at`、`cancel_reason`、通用字段。  
索引：`idx_appointment_landlord(store_id, status, start_at)`、`idx_appointment_tenant(tenant_user_id, start_at)`、`idx_appointment_listing(listing_id, start_at)`。

#### `user_notification`

字段：`id`、`user_id`、`notification_type`、`title`、`content`、`business_type`、`business_id`、`action_path`、`read_at`、`created_at`。  
索引：`idx_notification_user(user_id, read_at, created_at, id)`。

#### `notification_preference` 与 `store_appointment_rule`

`notification_preference` 字段：`id`、`user_id`、`channel(IN_APP)`、`preference_code(NEW_CONSULTATION/APPOINTMENT_CONTRACT/BILL_REPAIR)`、`enabled`、`updated_at`；唯一索引：`uk_notification_preference(user_id, channel, preference_code)`。  
`store_appointment_rule` 字段：`id`、`store_id`、`weekday`、`start_time`、`end_time`、`slot_minutes`、`capacity`、`enabled`、通用字段；索引：`idx_appointment_rule(store_id, weekday, enabled)`。租客端固定时间下拉仅是演示默认值，真实可约时段从该表和已有预约计算。

### 8.5 租约、退租和账单

#### `tenancy`

字段：`id`、`contract_no`、`store_id`、`property_id`、`listing_id`、`source_appointment_id`、`tenant_user_id`、`landlord_user_id`、`start_date`、`end_date`、`rent_amount_cents`、`deposit_amount_cents`、`payment_cycle_months`、`management_fee_cents`、`water_price_cents`、`electricity_price_cents`、`status(DRAFT/ACTIVE/EXPIRING/CHECKOUT_PENDING/TERMINATED/EXPIRED)`、`draft_hold_expires_at`、`signed_at`、`activated_at`、`terminated_at`、`termination_reason`、`version`、通用字段。  
唯一索引：`uk_contract_no(contract_no)`；索引：`idx_tenancy_tenant(tenant_user_id, status, end_date)`、`idx_tenancy_store(store_id, status, end_date)`、`idx_tenancy_property(property_id, status, start_date, end_date)`。

数据库难以用普通唯一索引完全约束日期重叠，激活时必须对 `property_id` 的有效租约查询加行锁并进行区间冲突校验。

#### `contract_attachment`

字段：`id`、`tenancy_id`、`file_id`、`attachment_type(CONTRACT/ID_COPY/OTHER)`、`display_name`、`sort_order`、`created_at`。  
索引：`idx_contract_attachment(tenancy_id, sort_order)`。

#### `checkout_request`

字段：`id`、`request_no`、`tenancy_id`、`tenant_user_id`、`store_id`、`desired_checkout_date`、`reason`、`status`、`inspection_note`、`deposit_amount_cents`、`deduction_amount_cents`、`refund_amount_cents`、`deduction_detail_json`、`requested_at`、`inspected_at`、`settled_at`、`version`、通用字段。  
索引：`idx_checkout_store(store_id, status, requested_at)`、`idx_checkout_tenant(tenant_user_id, requested_at)`；同一租约最多一条进行中的退租申请，由应用和事务锁保证。

#### `checkout_inspection_item`

字段：`id`、`checkout_request_id`、`item_name`、`condition_status(NORMAL/DAMAGED/MISSING)`、`deduction_amount_cents`、`note`、`evidence_file_id`、`sort_order`、`created_at`。  
索引：`idx_checkout_item(checkout_request_id, sort_order)`。原型中的空调、热水器、洗衣机等逐项验房和押金抵扣由本表承载，`checkout_request.deduction_detail_json` 仅保留结算快照。

#### `bill`

字段：`id`、`bill_no`、`store_id`、`tenancy_id`、`tenant_user_id`、`billing_period`（`CHAR(7)`，如 `2026-07`）、`total_amount_cents`、`due_date`、`status`、`issued_at`、`payment_reported_at`、`verified_at`、`verified_by`、`rejection_reason`、`version`、通用字段。  
唯一索引：`uk_bill_period(tenancy_id, billing_period)`；索引：`idx_bill_tenant(tenant_user_id, status, due_date)`、`idx_bill_store(store_id, status, due_date)`。

#### `bill_item`

字段：`id`、`bill_id`、`item_type(RENT/WATER/ELECTRICITY/MANAGEMENT/NETWORK/OTHER)`、`item_name`、`quantity DECIMAL(12,3)`、`unit_price_cents`、`amount_cents`、`previous_meter_value`、`current_meter_value`、`remark`、`sort_order`。  
索引：`idx_bill_item(bill_id, sort_order)`；账单总金额必须等于有效明细金额之和。

#### `bill_attachment` 与 `bill_status_log`

`bill_attachment` 字段：`id`、`bill_id`、`file_id`、`attachment_type(BILL_IMAGE/WATER_METER/ELECTRICITY_METER/OTHER)`、`sort_order`、`created_at`；唯一索引：`uk_bill_attachment(bill_id, file_id)`。  
`bill_status_log` 字段：`id`、`bill_id`、`from_status`、`to_status`、`operator_user_id`、`payment_report_id`、`note`、`created_at`；索引：`idx_bill_status_log(bill_id, created_at, id)`。账单详情的状态轨迹必须从本表返回，不从审计日志拼凑。

#### `payment_report`

字段：`id`、`bill_id`、`tenant_user_id`、`paid_amount_cents`、`paid_at`、`payment_method`、`payment_reference`、`remark`、`status(PENDING/VERIFIED/REJECTED)`、`verified_by`、`verified_at`、`rejection_reason`、`created_at`。  
索引：`idx_payment_bill(bill_id, created_at)`。每次驳回后允许新建报备，历史不可覆盖。

#### `payment_attachment`

字段：`id`、`payment_report_id`、`file_id`、`created_at`。唯一索引：`uk_payment_file(payment_report_id, file_id)`。

#### `expense_record`

字段：`id`、`expense_no`、`store_id`、`property_id`、`listing_id`、`tenancy_id`、`expense_date`、`category(REPAIR/CLEANING/UTILITY/COMMISSION/OTHER)`、`amount_cents`、`description`、`status(VALID/VOID)`、`attachment_file_id`、`created_by`、`voided_by`、`voided_at`、`void_reason`、通用字段。  
索引：`idx_expense_store(store_id, expense_date, status)`、`idx_expense_property(property_id, expense_date)`。本表满足产品需求中的支出、净利润统计，不能与租客账单混用。

### 8.6 报修与评价

#### `repair_ticket`

字段：`id`、`ticket_no`、`store_id`、`tenancy_id`、`listing_id`、`tenant_user_id`、`category_code`、`urgency(NORMAL/URGENT)`、`description`、`preferred_visit_at`、`contact_phone_cipher`、`assignee_name`、`assignee_phone_cipher`、`scheduled_at`、`status(PENDING/PROCESSING/AWAITING_CONFIRMATION/COMPLETED/CANCELLED)`、`completion_note`、`accepted_at`、`repaired_at`、`confirmed_at`、`version`、通用字段。  
索引：`idx_repair_store(store_id, status, created_at)`、`idx_repair_tenant(tenant_user_id, created_at)`。

#### `repair_attachment`

字段：`id`、`ticket_id`、`file_id`、`phase(SUBMITTED/PROCESSING/COMPLETED)`、`uploaded_by`、`created_at`。  
索引：`idx_repair_attachment(ticket_id, phase, created_at)`。

#### `repair_status_log`

字段：`id`、`ticket_id`、`from_status`、`to_status`、`operator_user_id`、`operator_role`、`note`、`created_at`。  
索引：`idx_repair_log(ticket_id, created_at, id)`；只追加，不更新和删除。

#### `review`

字段：`id`、`store_id`、`tenancy_id`、`listing_id`、`tenant_user_id`、`overall_rating`、`house_rating`、`service_rating`、`content`、`status(VISIBLE/HIDDEN)`、`hidden_reason`、`landlord_reply`、`replied_by`、`replied_at`、通用字段。  
唯一索引：`uk_review_tenancy(tenancy_id, tenant_user_id)`；索引：`idx_review_listing(listing_id, status, created_at)`。

### 8.7 运营、文件、审计和可靠事件

#### `community_config`

字段：`id`、`store_id`、`title`、`description`、`welcome_text`、`cover_file_id`、`join_type(LINK/QR_IMAGE/COPY_TEXT)`、`join_value`、`member_count_display`、`enabled`、`version`、通用字段。  
唯一索引：`uk_community_store(store_id)`。

#### `community_join_event`

字段：`id`、`store_id`、`community_config_id`、`user_id`（可空）、`event_type(CLICK/CONFIRMED_JOIN)`、`device_hash`（可空且不可逆）、`created_at`。  
索引：`idx_community_event(store_id, event_type, created_at)`。只有 `CONFIRMED_JOIN` 按去重规则更新展示人数；外部链接无法确认加入时只记录 `CLICK`。

#### `file_object`

字段：`id`、`storage_provider`、`bucket_name`、`object_key`、`original_name`、`mime_type`、`size_bytes`、`sha256`、`width`、`height`、`duration_seconds`、`visibility(PUBLIC/PRIVATE)`、`upload_status(PENDING/COMPLETED/FAILED)`、`business_type`、`business_id`、`uploaded_by`、`created_at`。  
唯一索引：`uk_file_object(storage_provider, bucket_name, object_key)`；索引：`idx_file_hash(sha256, size_bytes)`。

#### `audit_log`

字段：`id`、`request_id`、`operator_user_id`、`operator_role`、`store_id`、`action_code`、`business_type`、`business_id`、`before_json`、`after_json`、`ip`、`user_agent`、`result`、`created_at`。  
索引：`idx_audit_business(business_type, business_id, created_at)`、`idx_audit_operator(operator_user_id, created_at)`。敏感数据写日志前必须脱敏。

#### `business_activity` 与 `store_daily_metric`

`business_activity` 字段：`id`、`store_id`、`activity_type`、`business_type`、`business_id`、`actor_user_id`、`display_text`、`action_path`、`occurred_at`、`created_at`；索引：`idx_activity_store(store_id, occurred_at, id)`。它是仪表盘“最近动态”的脱敏投影，不替代审计日志。  
`store_daily_metric` 字段：`id`、`metric_date`、`store_id`、`impression_count`、`detail_view_count`、`conversation_start_count`、`appointment_count`、`tenancy_conversion_count`、`paid_income_cents`、`expense_cents`、`updated_at`；唯一索引：`uk_store_daily_metric(metric_date, store_id)`。用于仪表盘 30 天漏斗和财务趋势。

#### `outbox_event`

字段：`id`、`aggregate_type`、`aggregate_id`、`event_type`、`payload_json`、`status(PENDING/PUBLISHED/FAILED)`、`retry_count`、`next_retry_at`、`created_at`、`published_at`。  
索引：`idx_outbox_dispatch(status, next_retry_at, id)`。

### 8.8 核心关系

```text
store 1 ── N property_unit 1 ── N house_listing
property_unit(occupancy_status) 1 ── N tenancy（时间区间不能重叠）
house_listing 1 ── N listing_media / listing_feature / listing_daily_metric / conversation / appointment
tenant 1 ── N favorite / browse_history / conversation / appointment / verification
conversation 1 ── N chat_message
appointment 0..1 ── 1 tenancy（转合同后关联）
tenancy 1 ── N bill 1 ── N bill_item / bill_attachment / payment_report / bill_status_log
tenancy 1 ── N repair_ticket 1 ── N repair_status_log
tenancy 1 ── 0..1 review
tenancy 1 ── N checkout_request 1 ── N checkout_inspection_item（最多一条进行中）
store 1 ── N business_activity / store_daily_metric / expense_record
```

---

## 9. Redis 设计

Redis 仅承担可重建状态和高频访问，不作为财务与合同事实库。

| Key 模式 | 类型 | TTL/说明 |
|---|---|---|
| `auth:refresh:{userId}:{deviceId}` | String/Hash | 刷新令牌摘要，跟随登录有效期 |
| `auth:blacklist:{jti}` | String | 访问令牌剩余有效期，用于主动登出 |
| `listing:detail:{listingId}` | String(JSON) | 5–15 分钟，房源更新主动删除 |
| `listing:special:{storeId}` | String(JSON) | 2–5 分钟，特价变更主动删除 |
| `listing:query:{storeId}:{queryHash}` | String(JSON) | 1–3 分钟，只缓存热点第一页 |
| `tenant:home:{storeId}:{userIdOrGuest}` | String(JSON) | 30–60 秒，首页聚合短缓存 |
| `listing:view:count:{listingId}` | Counter | 浏览增量，定时批量回写 MySQL |
| `listing:view:dedup:{listingId}:{actor}` | String | 30 分钟，防页面刷新重复计数 |
| `chat:online:{userId}` | Set/Hash | WebSocket 心跳 TTL |
| `chat:unread:{userId}` | Hash | 可选加速，MySQL 仍保留可恢复未读数 |
| `idempotency:{userId}:{key}` | String(JSON) | 24 小时，保存关键请求结果摘要 |
| `rate:{scope}:{subject}` | Counter/令牌桶 | 登录、发消息、催缴、上传限流 |
| `lock:bill:{billId}` | Lock | 核销/驳回互斥，短 TTL + 看门狗 |
| `lock:tenancy:property:{propertyId}` | Lock | 激活租约和退租完成互斥 |
| `lock:appointment:{appointmentId}` | Lock | 确认、取消、转合同互斥 |

缓存采用 Cache-Aside：先查缓存，未命中查库回填；写数据库成功后删除相关缓存。删除失败要重试或通过 Outbox 补偿。禁止缓存包含未脱敏身份证、银行卡和完整手机号的 DTO。

---

## 10. 安全与隐私要求

### 10.1 认证与令牌

- Access Token 建议 30 分钟，Refresh Token 建议 14 天并按设备存储、每次刷新轮换。
- 密码使用 BCrypt/Argon2 哈希，不可逆加密；连续失败触发短时锁定和 IP/账号双维限流。
- 微信登录由服务端使用 `code` 调用官方接口，前端不得直接提交可伪造的 openid。
- WebSocket 握手必须鉴权，订阅目标由服务端绑定当前用户，禁止客户端指定其他用户 ID。

### 10.2 数据保护

- 手机、身份证、银行卡和紧急联系人采用应用层 AES-GCM/KMS 加密；查询使用独立 HMAC 哈希。
- 接口按场景脱敏，例如聊天和预约仅显示业务所需联系方式。
- 私有合同、付款凭证、报修图片使用短期签名 URL，不能永久公开。
- 日志、错误响应和审计差异禁止输出令牌、密码、微信 session key 和完整敏感字段。
- 文件上传校验 MIME、扩展名、大小和内容签名；限制图片/视频/PDF 类型，并预留病毒扫描钩子。
- 保留策略：浏览足迹默认 180 天、通知默认 1 年、聊天默认至少 2 年；合同及合同附件遵循产品“永久归档”口径，不做自动业务删除，实际物理保留年限上线前由法务/业务确认。临时未关联文件 24 小时清理。

### 10.3 关键操作审计

必须审计：房源发布/下架/特价、拉黑/解除、合同激活、账单核销/驳回/作废、退租结算、维修完工、评价隐藏、门店资料变更和管理员跨门店操作。

---

## 11. 定时任务与异步处理

| 任务 | 建议频率 | 作用 |
|---|---|---|
| 特价到期 | 每分钟 | 使过期特价停止展示并清缓存 |
| 房源/预约提醒 | 每 5 分钟 | 看房前提醒、过期预约处理 |
| 账单逾期 | 每日凌晨 | `PENDING_PAYMENT -> OVERDUE` |
| 合同到期提醒 | 每日 | 提前 30/7/1 天通知房东和租客 |
| 合同到期状态 | 每日 | 更新 `EXPIRING/EXPIRED`，不自动释放有争议房源 |
| 合同草稿占用到期 | 每 10 分钟 | 释放超时 `PENDING_SIGN` 房屋和未激活合同草稿 |
| 浏览数回写 | 每 1–5 分钟 | Redis 增量批量写 MySQL |
| 日指标汇总 | 每小时/每日 | 刷新 `listing_daily_metric`、`store_daily_metric` 和热度分 |
| Outbox 分发 | 每 5–10 秒 | 生成通知、会话系统消息和缓存失效事件 |
| 文件清理 | 每日 | 清理超时未完成上传和无引用临时文件 |
| 幂等记录清理 | Redis TTL | 自动过期，无需扫描数据库 |

多实例部署时任务必须加分布式锁；单次任务需要可重入、可补跑，并记录执行结果。

---

## 12. 非功能需求

### 12.1 性能目标

- 常规读接口 P95 小于 300ms，写接口 P95 小于 500ms，不含第三方登录和文件上传耗时。
- 房源列表默认 20 条，详情媒体只返回 URL 和元数据，不经应用服务器转发大文件。
- 演示基线支持 1,000 并发在线用户、100 QPS；架构保留水平扩容能力。
- 数据库查询必须有分页和索引，禁止生产环境无条件全表扫描和 N+1 查询。

### 12.2 可用性与容灾

- 健康检查区分 liveness/readiness；MySQL 或 Redis 不可用时明确降级和告警。
- Redis 缓存故障不得导致合同、账单等事实数据丢失；必要时限流后回源 MySQL。
- MySQL 每日全量备份 + binlog，演示环境至少保留 7 天；生产策略由部署方案确认。
- 文件存储开启版本/备份策略；删除业务引用时先软删除，异步延迟清理对象。

### 12.3 日志与监控

- 每个请求生成 `requestId`，贯穿 HTTP、WebSocket、数据库慢查询和异步事件。
- 指标：请求量/错误率/P95、在线连接、消息发送失败、缓存命中率、数据库连接池、慢 SQL、Outbox 积压、定时任务失败。
- 业务指标：房源曝光/详情浏览、收藏、会话、预约、合同转化、应收/已收、维修平均闭环时长。

---

## 13. 测试与验收方案

### 13.1 测试层次

- 单元测试：状态机、金额计算、筛选解析、权限规则、时间冲突和脱敏。
- Repository 集成测试：MySQL 8 Testcontainers，覆盖唯一约束、索引查询和事务锁。
- Redis 集成测试：幂等、限流、缓存失效、浏览计数和分布式锁。
- API 契约测试：所有接口的正常、无权限、校验失败和状态冲突场景。
- WebSocket 测试：鉴权、断线重连、消息去重、按房源隔离和未读数。
- 端到端联调：复用 Vue 3 租客端路由链路，并覆盖房东侧响应操作。

### 13.2 必验业务闭环

1. 房东建房源并上架 → 租客筛选到该房源 → 查看详情/足迹/收藏。
2. 房源详情 → 视频/图片元数据、费用、设施、电话、评分均来自接口；电话点击进入漏斗统计但不泄露非公开信息。
3. 租客发起会话 → 双方收发消息 → 同一房源的不同租客会话严格隔离。
4. 租客预约 → 房东确认/拒绝 → 双方会话出现预约卡片和通知 → 同一预约不可重复转合同。
5. 房东从预约生成合同草稿/一键激活 → 房屋占用、房源下架、租客合同页和首页列表同时正确变化。
6. 房东水电试算并附账单图片 → 租客查看图片/明细 → 付款报备 → 驳回一次 → 重新报备 → 核销。
7. 租客报修并附图 → 房东受理/派单/完工并附图 → 租客拒绝一次 → 再次完工并确认。
8. 租客退租 → 房东逐项验房、登记扣款并结算 → 合同终止 → 房源按配置恢复可租或保持下架。
9. 租客评价 → 房东回复 → 房源详情评分和评价数更新；隐藏评价不参与统计。
10. 房东更新社群、门店名称/地址、通知偏好、特价和海报 → 租客首页/房源页及房东后台立即反映。
11. 房东拉黑租客 → 新会话和预约被阻断 → 历史合同/账单仍可查看，履约通知仍可送达。

### 13.3 接口完成定义

每个接口进入“完成”必须同时满足：

- OpenAPI 文档、请求/响应示例和错误码齐全。
- 权限、门店数据隔离、参数校验和幂等策略已实现。
- 状态变化有事务、并发冲突处理和必要审计。
- 单元/集成测试通过，关键 SQL 已通过 `EXPLAIN` 检查。
- 前端联调无字段歧义；枚举和金额/时间格式符合本规范。

---

## 14. 环境、配置与交付前置

### 14.1 环境划分

| 环境 | 用途 | 数据要求 |
|---|---|---|
| `local` | 本地开发 | Docker MySQL/Redis，可重置 Mock 数据 |
| `test` | 自动化和接口测试 | 独立库，测试后清理 |
| `staging` | 前后端联调和演示彩排 | 固定演示账号和稳定媒体资源 |
| `prod` | 正式环境 | 密钥托管、备份、监控、最小权限 |

配置项至少包含：数据库连接、Redis、JWT 密钥与有效期、微信 AppId/Secret、文件存储、CORS 白名单、上传限制、门店默认 ID、公开域名、日志级别和定时任务开关。所有密钥从环境变量/密钥中心注入，不进入 Git。

### 14.2 数据库迁移顺序

1. 用户、身份、角色、门店。
2. 物理房屋、房源、媒体、设施。
3. 收藏、足迹、会话、消息、预约、通知。
4. 租约、退租、账单、付款报备。
5. 报修、评价、社群、通知偏好、文件、审计、Outbox、经营日报。
6. 字典、门店认证/收款账户、支出记录和演示种子数据。

### 14.3 演示数据基线

- `tenant-vue/src/data.ts` 的 12 套房源是 Vue 联调基线，必须逐字段迁移并保持 ID 对照；`demo.html` 的 50 套房源作为可选扩展种子，不能直接替换这 12 套导致视觉演示数量变化。
- 建立一个演示门店、一个房东账号、一个租客微信替代账号。
- 至少准备：3 个收藏、3 条足迹、3 个房源会话、1 个待确认预约、1 个已确认待转合同预约、3 种账单状态及账单图片、4 种维修状态及报修图片、1 个有效合同、1 个退租案例、若干评价、门店认证/收款展示资料和三项通知偏好。
- 外部 Unsplash/Mixkit URL 可保留为 `external_url`；正式上传后切换到文件对象，不改变接口字段。
- 提供可重复执行的 seed 脚本，默认密码只允许在非生产环境启用。

### 14.4 建议开发阶段

| 阶段 | 交付内容 |
|---|---|
| P0 基础设施 | 工程骨架、鉴权、统一响应、Flyway、Redis、审计和文件抽象 |
| P1 找房闭环 | 房源查询/管理、筛选、详情、收藏、足迹、对比、社群 |
| P2 沟通转化 | 会话、WebSocket、通知、预约和房东线索 |
| P3 租务财务 | 合同、房源占用、账单、付款报备和核销 |
| P4 售后闭环 | 报修、退租、评价、黑名单和仪表盘 |
| P5 验收上线 | 性能、安全、备份恢复、演示数据和端到端验收 |

### 14.5 进入编码前必须交付的输入物

| 产物 | 所有人 | 完成标准 |
|---|---|---|
| OpenAPI v1 初稿 | 后端 | 覆盖第 7 章全部接口，字段、枚举、示例、错误码和鉴权标签齐全 |
| 枚举与字典清单 | 产品/后端/前端 | 房源、预约、租约、账单、报修、评价状态及中文显示文案唯一且冻结 |
| ER 图与 Flyway 拆分清单 | 后端 | 覆盖第 8 章表关系、索引、迁移顺序和种子数据依赖 |
| 状态迁移用例表 | 后端/测试 | 每个状态迁移写明角色、前置状态、事务写入、通知和错误码 |
| 演示账号与数据剧本 | 产品/测试 | 账号、初始状态、每一步操作和预期双端画面可重复执行 |
| 文件存储与权限清单 | 运维/后端 | Bucket、MIME、大小、公开/私有、签名 URL 时长和清理策略已确认 |
| 接口 Mock 对照表 | 前端/后端 | 将 Vue `data.ts`/Pinia 字段逐项映射到 DTO，标明删除的硬编码字段 |

---

## 15. 已确定的设计决策

1. 后端同时支持租客端和房东后台，不为两端各建一套重复数据。
2. 首期采用模块化单体，不拆微服务，不强制引入 MQ 和搜索引擎。
3. MySQL 是唯一事实库，Redis 只做缓存、令牌、计数、幂等、限流和锁。
4. 金额统一整数分，时间统一 ISO 8601，ID 对前端统一字符串。
5. 聊天按“租客 + 房东 + 房源”隔离，历史消息持久化，WebSocket 与 REST 共用发送用例。
6. 收藏最多三套是前端对比选择规则；服务端对比接口也限制最多三个 ID，但收藏总数不设三套限制。
7. “已付款”是租客报备，只有房东核销才能进入 `PAID`。
8. 报修必须经租客确认才能完成；退租完成后才释放房源。
9. 合同附件和付款凭证为私有文件，房源公开媒体可为公开文件。
10. 原型中的 Mock 状态在后端均通过明确状态机和审计记录实现，不使用前端临时状态替代。
11. 房源发布状态与物理房屋占用状态分离；租客端可见必须同时满足“已发布 + 空置”。
12. 所有账单金额以租约费用快照和服务端读数试算为准，不以房源当前价格或前端计算结果为准。

---

## 16. 开发启动前检查清单

- [ ] 产品、前端、后端共同确认本文件的页面映射、状态机和范围。
- [ ] 确认微信登录的实际运行载体和 AppId；演示环境确定替代登录方案。
- [ ] 确认单门店还是首期多门店；本设计按多门店数据隔离实现。
- [ ] 确认文件存储选型、域名、最大图片/视频/PDF 大小。
- [ ] 确认合同编号、账单编号、押付方式和水电费计算业务规则。
- [ ] 确认海报生成方式（后端模板渲染或前端 Canvas）及小程序码来源；本接口设计默认后端生成可下载图片。
- [ ] 确认账单图片、表读数照片、付款凭证、报修图片、合同附件的文件大小、保留期和访问权限。
- [ ] 确认合同草稿是否占房、占房时长以及退租后自动重新上架的门店默认值。
- [ ] 确认退租扣款、退款仅记录还是需要未来接支付渠道。
- [ ] 确认房东员工权限是否首期细分；未确认前按门店成员同权限实现。
- [ ] 评审数据库命名、索引、加密字段和数据保留周期。
- [ ] 根据本接口目录生成 OpenAPI 初稿，并让 Vue 端以类型生成方式联调。
- [ ] 创建 Flyway 基线、演示 seed 数据和可重复初始化流程。
- [ ] 建立 CI：编译、单元测试、集成测试、OpenAPI 变更检查和数据库迁移校验。

本检查清单全部关闭后，后端才进入正式业务开发阶段。
