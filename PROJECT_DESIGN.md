# 二手交易平台实施说明（订单制）

## 第一步：项目整体设计说明
- 架构：`second-hub-server`(Spring Boot 3.5.2) + `second-hub-client`(微信小程序) + `second-hub-admin`(Vue3+Element Plus)。
- 角色：普通用户、管理员/审核员。
- 核心流程：微信登录自动注册 -> 用户发布商品 -> 管理员审核 -> 用户浏览下单 -> 模拟支付 -> 买卖双方确认完成订单。
- 设计原因：
  - 前后端分离便于多端并行开发。
  - `/api/user/**` 与 `/api/admin/**` 分域可做权限隔离。
  - 订单状态机保障交易流可追踪、可审计。

## 第二步：数据库表设计与 SQL
- SQL 文件：`second-hub-server/src/main/resources/sql/db_second_hub.sql`
- 数据库名：`db_second_hub`
- 核心表：`user`、`admin_user`、`category`、`goods`、`goods_image`、`goods_favorite`、`goods_comment`、`trade_order`、`goods_report`、`notice`、`goods_audit`
- 设计原因：
  - 删除 `file_metadata`，避免第一版过度设计。
  - 商品图片单独 `goods_image`，保持主表轻量。
  - 订单表包含支付状态+双方确认位，满足“用户直接下单”与“双方确认完成”。

## 第三步：接口设计
- 用户端：
  - 登录：`POST /api/user/auth/wx-login`
  - 商品：`/api/user/goods`（发布/编辑/删除/下架/列表/详情/我的）
  - 收藏：`POST|DELETE|GET /api/user/favorites...`
  - 留言：`POST /api/user/comments`、`GET /api/user/comments/{goodsId}`
  - 订单：`/api/user/orders`（下单、支付、买卖双方确认、取消、我的订单）
  - 举报：`POST /api/user/reports`
- 管理端：
  - 登录：`POST /api/admin/auth/login`
  - 商品审核：`/api/admin/goods/pending`、`/api/admin/goods/{id}/audit`
  - 分类/用户/举报/公告/订单/统计：`/api/admin/categories`、`/api/admin/users`、`/api/admin/reports`、`/api/admin/notices`、`/api/admin/orders`、`/api/admin/dashboard`
- 设计原因：按职责拆分接口，降低耦合并提升权限控制清晰度。

## 第四步：前端实现
- 小程序：已实现登录、商品列表、详情、发布、收藏、订单、我的页骨架。
- 管理端：已实现登录、仪表盘、商品审核、分类、用户、举报、公告、订单页骨架。
- 设计原因：先把主流程打通，再逐页做体验增强。

## 第五步：Spring Boot 骨架
- 已实现：统一返回 `ApiResponse`、全局异常、参数校验、JWT 拦截、Knife4j 文档、本地文件上传。
- 设计原因：项目早期先搭基础设施可显著降低后续返工。

## 第六步：后端核心业务实现
- 已实现：登录、商品、收藏、留言、订单、审核、分类、用户管理、举报处理、公告、统计。
- 订单状态：`PENDING_PAYMENT -> PAID -> SELLER_CONFIRMED/BUYER_CONFIRMED -> COMPLETED`，可取消。
- 设计原因：按业务闭环实现，确保功能可运行而不是仅有接口定义。

## 第七步：Redis 缓存与优化建议
- 已实现：看板概览缓存（30秒）
- 建议继续：商品详情缓存、热门列表缓存、订单幂等键、防重复提交、分布式限流。
- 设计原因：先缓存高频读接口，再逐步扩展风控与并发保障。

## 约束确认
- MySQL 密码：`1234`
- 种子管理员/用户密码：`md5(123456)` -> `e10adc3949ba59abbe56e057f20f883`
