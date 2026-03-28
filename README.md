# 社区临期食品低碳减损平台（Food Saving Platform）

前后端分离的演示/教学项目：居民选购临期商品、商家核销、管理员运营；订单与碳减排挂钩，并提供 **碳积分（Carbon Coins）** 与 **游戏化商城**（虚拟树、徽章、优惠券），优惠券可在 **购物车结算时自动抵扣实付**。

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、Spring Security、JWT、MyBatis-Plus、MySQL、Redis |
| 前端 | Vue 3、Vite、Pinia、Vue Router |

---

## 功能概览

- **居民**：选择社区、浏览商品、购物车、下单（钱包支付）、订单与核销码、**低碳中心**（碳积分与流水）、**碳积分商城**（兑换树/徽章/券）、购物车选用 **未使用优惠券** 自动减价  
- **商家**：商品管理、订单、核销、评价、仪表盘  
- **管理员**：仪表盘、商户管理（列表/详情统计/删除）、社区与分类、全站订单、操作日志、站内提醒等  

---

## 环境要求

- JDK 17  
- Maven 3.9+  
- Node.js 18+  
- MySQL 8+  
- Redis 6+（默认 `localhost:6379`）  

---

## 数据库初始化（首次必做）

1. 在 MySQL 中创建库（若尚无）：

   ```sql
   CREATE DATABASE IF NOT EXISTS food_saving CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 按顺序执行（均在仓库 `docs/sql/` 下）：

   | 脚本 | 说明 |
   |------|------|
   | `init.sql` | 基础表与示例数据 |
   | `schema-compat.sql` | 兼容补丁（补列等） |
   | `demo-seed.sql` | 演示种子数据（推荐） |
   | `carbon-gamification.sql` | **碳积分兑换表、用户优惠券表、订单优惠字段**（游戏化与下单用券） |

   **推荐**：使用下方 `setup-demo.ps1` 一键执行上述脚本（含 `carbon-gamification.sql`）。

   手动执行示例（请按本机修改用户、密码、路径）：

   ```powershell
   mysql -u root -p food_saving < docs/sql/init.sql
   mysql -u root -p food_saving < docs/sql/schema-compat.sql
   mysql -u root -p food_saving < docs/sql/demo-seed.sql
   mysql -u root -p food_saving < docs/sql/carbon-gamification.sql
   ```

   说明：后端启动时也会对部分表做 **自动补表/补列**；已跑过 `carbon-gamification.sql` 或与之一致时，重复执行一般安全（`CREATE IF NOT EXISTS`、按列检测的 `ALTER`）。

---

## 推荐：一键初始化演示库（Windows）

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-demo.ps1
```

默认连接：`localhost:3306`，用户 `root`，密码 `EA7music666`，库名 `food_saving`。  
覆盖密码示例：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-demo.ps1 -DbPassword "你的密码"
```

该脚本会依次执行：`init.sql` →（若存在）根目录 `fix-merchant-table.sql` → `schema-compat.sql` → `demo-seed.sql` → **`carbon-gamification.sql`**（若文件存在）。

---

## 启动顺序

1. 启动 **Redis**（默认端口 `6379`）  
2. 启动 **后端**（默认 `8080`）  

   ```powershell
   cd backend
   mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
   ```

3. 启动 **前端**（默认 `5173`）  

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

4. 浏览器访问：**http://localhost:5173**

---

## 一键启动前后端（可选）

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1
```

常用参数：`-InitDb`（启动前跑库初始化）、`-DbHost`、`-DbPort`、`-DbUser`、`-DbPassword`、`-DbName`、`-JwtSecret`、`-FrontendPort`、`-BackendPort`。  
示例：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1 -InitDb `
  -DbUser root -DbPassword EA7music666 -DbName food_saving `
  -FrontendPort 5173 -BackendPort 8080
```

---

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 居民 | `consumer` | `consumer123` |
| 商家（演示一） | `merchant1` | `merchant1123` |
| 商家（演示二） | `merchant2` | `merchant2123` |

说明：请保证数据库中已存在上述两个商家账号及对应 `biz_merchant` 数据。若本地初始化脚本仍只写入 `merchant` 单账号，需自行在 `sys_user` 中增补 `merchant1` / `merchant2` 或调整 `docs/sql` 种子与上表一致。

---

## 建议演示流程

1. `consumer` 登录 → 选社区 → 加购 → **购物车**可选用碳积分商城兑换的 **优惠券**（单笔结算、单条购物车记录时生效）→ 下单  
2. `merchant1` 或 `merchant2` 登录商家端 → 核销对应订单  
3. `consumer` → **低碳中心** 查看碳积分；**碳积分商城** 兑换虚拟树/徽章/新券  
4. `admin` 登录 → 仪表盘 / 商户 / 订单 等  

---

## 主要 HTTP 接口（节选）

**认证**  
`POST /api/auth/login`、`POST /api/auth/register`、`GET /api/auth/info`

**居民**  
`GET /api/consumer/communities`、`GET /api/consumer/products`  
`GET /api/consumer/cart`、`POST /api/consumer/cart/checkout`（body 可含 `cartIds`、`couponCode`）  
`POST /api/consumer/order/create`（body 可含 `couponCode`）  
`GET /api/consumer/orders`、`GET /api/consumer/carbon`  
`GET /api/consumer/gamification/catalog`、`GET /api/consumer/gamification/state`、`POST /api/consumer/gamification/redeem`  
`GET /api/consumer/coupons`（未使用优惠券列表）

**商家**  
`GET /api/merchant/orders`、`POST /api/merchant/order/verify` 等  

**管理员**  
`GET /api/admin/dashboard/stats`、`GET /api/admin/merchants`、`GET /api/admin/merchants/{id}/stats`、`DELETE /api/admin/merchants/{id}` 等  

完整列表以 `backend` 控制器源码为准。

---

## API 冒烟脚本

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-api.ps1
```

可选：`-BaseUrl`、`-AdminUser`、`-AdminPassword` 等。

---

## 配置与安全

`backend/src/main/resources/application.yml` 支持环境变量覆盖，避免生产硬编码：

| 变量 | 说明 |
|------|------|
| `DB_URL` | JDBC 连接串 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 |
| `JWT_SECRET` / `JWT_EXPIRATION` | JWT |
| `ALLOW_TEST_ENDPOINTS` | 是否开放 `/api/test/**` 等调试接口（生产请 `false`） |

- `/api/test/**`、`/api/init/**`、`/api/debug/**` 默认不对外；仅当 `ALLOW_TEST_ENDPOINTS=true` 时开放（开发常用）。  
- 使用 `dev` profile 时，`application-dev.yml` 可配合本地调试。  

仅当前 PowerShell 会话设置示例：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:ALLOW_TEST_ENDPOINTS="false"
```

---

## 仓库结构（简）

```
backend/          Spring Boot 工程
frontend/         Vue 3 + Vite 工程
docs/sql/         数据库脚本（init、兼容补丁、演示种子、游戏化/优惠券）
scripts/          setup-demo、start-all、smoke-api 等
```

---

## 推送到 GitHub（推荐 HTTPS）

在 GitHub 上新建空仓库后，用 **HTTPS 地址** 作为 `origin`，避免本机配置 SSH 密钥。

1. **复制仓库地址**（仓库页 **Code → HTTPS**），形如：  
   `https://github.com/<用户名>/<仓库名>.git`

2. **绑定远程并推送**（在项目根目录执行，分支名按实际修改，常见为 `main` 或 `master`）：

   ```powershell
   cd C:\projects\food-saving-platform-new

   git remote add origin https://github.com/<用户名>/<仓库名>.git
   # 若已添加过 origin，可改为：
   # git remote set-url origin https://github.com/<用户名>/<仓库名>.git

   git branch -M main
   git add .
   git commit -m "chore: initial push"
   git push -u origin main
   ```

3. **登录凭据**：GitHub 已不支持「账号 + 登录密码」推送。请在 **Settings → Developer settings → Personal access tokens** 创建 **PAT**，勾选 **`repo`**。  
   在提示输入密码时，**密码处粘贴 PAT**；用户名填你的 GitHub 用户名。Windows 可选用 **Git Credential Manager** 保存凭据，后续可直接 `git push`。

4. **若远程曾设为 SSH**（`git@github.com:...`），可改为 HTTPS：

   ```powershell
   git remote set-url origin https://github.com/<用户名>/<仓库名>.git
   git remote -v
   ```

---

## 许可与声明

本项目用于学习/演示；生产部署前请修改默认密码、JWT 密钥、数据库权限，并完成安全审计与备份策略。
