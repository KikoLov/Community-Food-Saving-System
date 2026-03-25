# Food Saving Platform

社区临期食品低碳减损系统，前后端分离项目：
- 后端：Spring Boot + Spring Security + JWT + MyBatis-Plus
- 前端：Vue 3 + Vite + Pinia + Vue Router

## 1) 本地一键启动顺序

### 前置依赖
- JDK 17
- Maven 3.9+
- Node.js 18+
- MySQL 8+
- Redis 6+

### 推荐启动步骤
1. 初始化数据库（首次）
   - 在 MySQL 中创建库：`food_saving`
   - 执行 SQL：`docs/sql/init.sql`（如有补丁脚本，再执行根目录下 `init-db.sql`、`fix-merchant-table.sql`）
2. 启动 Redis（默认 `localhost:6379`）
3. 启动后端（端口 `8080`）
   - `cd backend`
   - `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
4. 启动前端（端口 `5173`）
   - `cd frontend`
   - `npm install`
   - `npm run dev`
5. 访问地址
   - 前端：`http://localhost:5173`
   - 后端健康检查（示例）：`http://localhost:8080/api/auth/info`（需登录态）

### 数据库快速初始化（推荐）
已提供一键脚本（Windows PowerShell）：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-demo.ps1
```

默认会连接：
- Host: `localhost`
- Port: `3306`
- User: `root`
- Password: `EA7music666`
- DB: `food_saving`

可覆盖参数（示例）：
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup-demo.ps1 -DbPassword "你的密码"
```

初始化完成后可直接登录：
- `admin`（管理员）
- `consumer`（居民）
- `merchant`（商家）

默认密码：
- `admin` -> `admin123`
- `consumer` -> `consumer123`
- `merchant` -> `merchant123`

### 一键启动（后端+前端）
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1
```

可选参数：
- `-InitDb`: 启动前执行数据库初始化（会调用 `setup-demo.ps1`）
- `-DbHost` `-DbPort` `-DbUser` `-DbPassword` `-DbName`: 覆盖数据库连接
- `-JwtSecret`: 覆盖 JWT 密钥（默认临时值，生产需修改）
- `-FrontendPort` `-BackendPort`: 覆盖前后端端口

示例：
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\start-all.ps1 -InitDb `
  -DbUser root -DbPassword EA7music666 -DbName food_saving `
  -FrontendPort 5173 -BackendPort 8080
```

## 2) 核心接口冒烟测试清单

建议按“认证 -> 居民 -> 商家 -> 管理员”顺序验证。

### 认证模块
- `POST /api/auth/register` 注册新用户
- `POST /api/auth/login` 登录并获取 token
- `GET /api/auth/info` 用 token 获取当前用户信息
- `POST /api/auth/logout` 退出登录

### 居民端
- `GET /api/consumer/communities` 社区列表
- `POST /api/consumer/community/bind` 绑定社区
- `GET /api/consumer/products` 商品列表
- `POST /api/consumer/cart/add` 加入购物车
- `GET /api/consumer/cart` 购物车列表
- `POST /api/consumer/order/create` 创建订单
- `GET /api/consumer/orders` 我的订单
- `GET /api/consumer/carbon` 低碳中心

### 商家端
- `GET /api/merchant/profile` 商户资料
- `POST /api/merchant/products` 新增商品
- `GET /api/merchant/products` 商品列表
- `GET /api/merchant/products/warning` 预警商品
- `GET /api/merchant/orders` 商家订单
- `POST /api/merchant/order/verify` 核销订单

### 管理员端
- `GET /api/admin/merchants` 商户审核列表
- `PUT /api/admin/merchant/{id}/audit` 审核商户
- `GET /api/admin/communities` 社区管理
- `GET /api/admin/categories` 分类管理
- `GET /api/admin/dashboard/stats` 仪表盘统计
- `GET /api/admin/orders` 全部订单

### 演示主流程（建议）
1. 用 `consumer` 登录，进入商品页下单
2. 用 `merchant` 登录，进入核销页核销订单
3. 返回 `consumer` 查看“低碳中心”积分变化
4. 用 `admin` 登录查看商户/订单/仪表盘数据

### 一键 API 冒烟回归（自动化）
已提供脚本：`scripts/smoke-api.ps1`，会自动验证以下链路：
- 三角色登录鉴权
- 站内提醒接口（列表 + 未读数）
- 居民下单幂等（重复下单应返回同一订单ID）
- 商家核销预览
- 管理员订单与操作审计接口

执行方式：
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-api.ps1
```

可选参数（示例）：
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\smoke-api.ps1 `
  -BaseUrl "http://localhost:8080" `
  -AdminUser "admin" -AdminPassword "admin123"
```

## 3) 安全基线（已在代码中落地）

后端 `application.yml` 现已改为环境变量优先，避免明文凭据硬编码：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`
- `ALLOW_TEST_ENDPOINTS`（默认 `false`）

### 关键说明
- `/api/test/**`、`/api/init/**`、`/api/debug/**` 默认不再对外放行。
- 仅当显式设置 `ALLOW_TEST_ENDPOINTS=true` 时才开放（建议只在开发环境）。
- 使用 `dev` profile 时，`application-dev.yml` 会默认打开测试接口，便于本地调试。

### PowerShell 示例（仅当前会话有效）
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="replace-with-a-long-random-secret"
$env:ALLOW_TEST_ENDPOINTS="false"
```
