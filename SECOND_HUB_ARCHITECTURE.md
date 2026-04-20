# Second Hub 项目架构说明

## 1. 项目概览

`second-hub` 是一个二手交易平台，采用前后端分离的三端结构：

- `second-hub-server`：Spring Boot 后端服务（API、鉴权、业务、数据访问）
- `second-hub-client`：微信小程序用户端（浏览、发布、下单、互动）
- `second-hub-admin`：Vue3 + Element Plus 管理端（审核、运营、管理）

核心业务流：微信登录 -> 发布商品 -> 管理员审核 -> 首页浏览 -> 下单支付 -> 买卖双方确认完成。

## 2. 数据库设计

### 2.1 数据库与连接约束

- 数据库名：`db_second_hub`
- SQL 初始化文件：`second-hub-server/src/main/resources/sql/db_second_hub.sql`
- MySQL 连接：`127.0.0.1:3306`
- 用户名：`root`
- 密码：`1234`

> 项目约定：数据库名以 `db_` 开头。

### 2.2 核心表分组

用户与权限域：
- `user`：普通用户
- `admin_user`：管理员

商品域：
- `category`：商品分类
- `goods`：商品主表
- `goods_image`：商品图片
- `goods_favorite`：商品收藏
- `goods_comment`：商品留言

交易域：
- `trade_order`：交易订单

运营与风控域：
- `goods_report`：商品举报
- `notice`：公告
- `goods_audit`：审核记录

### 2.3 关键关系与设计说明

- 商品主从关系：`goods (1) -> (N) goods_image`
- 用户与商品：`user (1) -> (N) goods`
- 订单关联：`trade_order` 关联 `goods_id`、`buyer_id`、`seller_id`
- 审核链路：`goods_audit` 记录商品审核动作和结果
- 举报链路：`goods_report` 记录举报内容与处理状态
- 逻辑删除：核心表统一使用 `is_deleted` 字段

### 2.4 状态字段（核心）

- 商品状态枚举：`DRAFT`、`PENDING`、`APPROVED`、`REJECTED`、`OFFLINE`、`SOLD`
DRAFT：草稿状态。商品仅保存未提交审核，通常对外不可见。
PENDING：待审核状态。用户已提交，等待管理员审核，通常不在首页公开展示。
APPROVED：审核通过状态。商品可正常上架展示、可被浏览和下单。
REJECTED：审核驳回状态。商品未通过审核，需要用户修改后再次提交。
OFFLINE：已下架状态。商品从可售列表移除，不再对普通浏览开放。
SOLD：已售出状态。商品交易已完成，不可再次下单。
- 订单状态/支付状态：在 `trade_order` 内分别维护 `order_status` 与 `pay_status`

## 3. 接口设计

### 3.1 接口分域

- 用户私有域：`/api/user/**`
- 用户公共域：`/api/user/public/**`
- 管理域：`/api/admin/**`

### 3.2 用户端接口（按控制器）

`UserAuthController`（`/api/user/auth`）
- `POST /wx-login`：微信登录

`UserFileController`（`/api/user/files`）
- `POST /upload`：用户上传文件

`UserGoodsController`（`/api/user/goods`）
- `POST /`：创建商品
- `PUT /{goodsId}`：编辑商品
- `DELETE /{goodsId}`：删除商品
- `POST /{goodsId}/offline`：下架商品
- `POST /{goodsId}/online`：上架（转审核中）
- `GET /list`：商品列表
- `GET /my`：我的发布
- `GET /{goodsId}`：商品详情

`UserInteractionController`（`/api/user`）
- `POST /favorites/{goodsId}`：收藏
- `DELETE /favorites/{goodsId}`：取消收藏
- `GET /favorites`：收藏列表
- `POST /comments`：发布留言
- `GET /comments/{goodsId}`：留言列表
- `POST /reports`：发起举报

`UserOrderController`（`/api/user/orders`）
- `POST /`：创建订单
- `POST /{orderId}/pay`：模拟支付
- `POST /{orderId}/seller-confirm`：卖家确认
- `POST /{orderId}/buyer-confirm`：买家确认
- `POST /{orderId}/cancel`：取消订单
- `GET /{orderId}`：订单详情
- `GET /my`：我的订单

`UserPublicController`（`/api/user/public`）
- `GET /categories`：分类列表
- `GET /notices`：公告列表

### 3.3 管理端接口（按控制器）

`AdminAuthController`（`/api/admin/auth`）
- `POST /login`：管理员登录

`AdminCategoryController`（`/api/admin/categories`）
- `GET /`、`POST /`、`PUT /{id}`、`DELETE /{id}`

`AdminDashboardController`（`/api/admin/dashboard`）
- `GET /overview`、`GET /trend`

`AdminFileController`（`/api/admin/files`）
- `POST /upload`

`AdminGoodsController`（`/api/admin/goods`）
- `GET /pending`、`POST /{goodsId}/audit`、`POST /{goodsId}/offline`

`AdminNoticeController`（`/api/admin/notices`）
- `GET /`、`POST /`、`PUT /{id}`、`DELETE /{id}`

`AdminOrderController`（`/api/admin/orders`）
- `GET /`、`POST /{orderId}/cancel`

`AdminReportController`（`/api/admin/reports`）
- `GET /`、`POST /{reportId}/handle`

`AdminUserController`（`/api/admin/users`）
- `GET /`、`POST /{userId}/status`

### 3.4 统一响应与鉴权

统一响应结构：`ApiResponse<T>`
- `code`：业务码（成功为 `0`）
- `message`：描述信息
- `data`：业务数据
- `timestamp`：响应时间

鉴权机制：
- 请求头：`Authorization: Bearer <token>`
- 拦截器：`AuthInterceptor`
- 路由级角色隔离：`/api/user/**` 仅 `USER`，`/api/admin/**` 仅 `ADMIN`
- 免鉴权路径：登录接口、公共查询接口、文档接口、上传静态资源路径等

## 4. 项目架构

### 4.1 后端分层

`second-hub-server/src/main/java/com/nie/secondhub` 主要包结构：

- `controller`：API 入口层
- `service` / `service.impl`：业务编排层
- `mapper`：MyBatis-Plus 数据访问层
- `entity`：数据库实体
- `dto`：入参对象
- `vo`：出参对象
- `security`：JWT、拦截器
- `config`：MVC、存储、文档、配置类
- `common`：通用响应、异常、枚举、上下文
- `util`：工具类

### 4.2 基础设施与中间件

- 持久化：MySQL + MyBatis-Plus
- 缓存：Redis（`database: 4`）
- 文件存储：本地文件（`uploads`），通过 Spring 资源映射对外访问
- API 文档：springdoc + Knife4j
- 参数校验：Jakarta Validation
- 逻辑删除：MyBatis-Plus 统一配置 `is_deleted`

### 4.3 关键运行配置

来自 `application.yml`：

- 服务端口：`8080`
- 文件上传限制：单文件 `5MB`，请求总大小 `30MB`
- JWT：`secret`、过期时间 `86400s`、`issuer`
- 本地存储：`local-root=uploads`，URL 前缀 `/uploads/`
- CORS：全局放开（headers/methods/origins）

### 4.4 典型业务链路示例

发布与审核链路：
1. 用户调用 `POST /api/user/goods` 发布商品（状态 `PENDING`）
2. 管理员在 `GET /api/admin/goods/pending` 获取待审核列表
3. 管理员调用 `POST /api/admin/goods/{goodsId}/audit` 审核
4. 审核通过后商品进入可售状态，在首页列表中展示

交易链路：
1. 用户在详情页创建订单 `POST /api/user/orders`
2. 买家支付 `POST /api/user/orders/{orderId}/pay`
3. 买卖双方分别确认 `seller-confirm` / `buyer-confirm`
4. 双方完成后订单闭环

## 5. 快速索引

- 数据库 SQL：`second-hub-server/src/main/resources/sql/db_second_hub.sql`
- 服务配置：`second-hub-server/src/main/resources/application.yml`
- 用户端控制器：`second-hub-server/src/main/java/com/nie/secondhub/controller/user`
- 管理端控制器：`second-hub-server/src/main/java/com/nie/secondhub/controller/admin`
- 设计说明（历史）：`PROJECT_DESIGN.md`