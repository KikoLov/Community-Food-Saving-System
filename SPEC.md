# 社区临期食品低碳减损系统 - 产品设计与需求文档 (PRD)

## 1. 项目概述与目标

### 1.1 项目名称
社区临期食品低碳减损系统 (Food Saving Platform)

### 1.2 应用类型
纯 Web 端应用（B/S 架构）

### 1.3 核心目标
打造一个连接社区商户与居民的低碳交易 Web 平台。商户可通过网页端管理和低价处理临期食品，居民可通过网页端浏览、购买高性价比食品并获取自提码，平台负责整体调度与数据统计。

---

## 2. 核心技术栈

### 后端技术栈
- **框架**: Spring Boot 3.2.0
- **ORM**: MyBatis-Plus 3.5.5
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0+
- **缓存**: Redis (库存高并发扣减、预警缓存、Session管理)
- **工具**: Lombok, Hutool

### 前端技术栈
- **框架**: Vue.js 3.x
- **UI组件库**: Element Plus
- **构建工具**: Vite
- **HTTP客户端**: Axios
- **路由**: Vue Router 4.x
- **状态管理**: Pinia

---

## 3. 系统角色与功能模块

### 3.1 居民端 (买家角色) - role_code: consumer

#### 3.1.1 社区切换与商品大厅
- 用户登录后，手动选择或绑定自己所在的社区
- 系统展示该社区内商户发布的临期食品列表
- 支持按分类筛选、按距离排序

#### 3.1.2 购物车与下单
- 浏览商品（明确标注保质期截止时间）
- 加入购物车、Web 端模拟支付/积分扣减下单

#### 3.1.3 订单与核销码
- 订单生成后，系统分配一个 6 位数的取货核销码
- 用户凭此码到线下门店自提

#### 3.1.4 低碳中心
- 查看个人累计积累的"低碳积分"
- 查看对应的碳减排量估算值

### 3.2 商家端 (卖家角色) - role_code: merchant

#### 3.2.1 店铺与资质管理
- 维护店铺基本信息（营业时间、详细地址等）
- 上传营业执照供平台审核

#### 3.2.2 临期商品管理
- 单条录入临期食品信息
- Excel 批量导入临期食品信息
- 设置原价、折扣价、库存量及精确的失效时间

#### 3.2.3 库存预警
- 当商品距离过期时间小于设定阈值时，在商家控制台首页高亮预警
- 提示降价或下架

#### 3.2.4 订单核销工作台
- 提供一个快速核销页面
- 商家输入居民提供的 6 位核销码校验
- 确认交货，完成订单闭环

### 3.3 平台管理端 (Admin 超级管理员) - role_code: admin

#### 3.3.1 基础系统管理
- 用户管理、角色管理、菜单管理、部门（社区）管理、字典管理

#### 3.3.2 业务数据审核
- 审核商户入驻申请
- 管理食品分类字典（如：烘焙、生鲜、便利店等）

#### 3.3.3 低碳数据大屏（BI）
- 统计全平台交易总额
- 累计挽救的食品总重量（kg）
- 折算的碳减排总量（CO2）
- 通过 ECharts 图表在首页控制台展示

---

## 4. 核心业务流程

### 4.1 商品上架流程
1. 商家登录 Web 后台
2. 录入商品信息
3. 系统校验失效时间必须晚于当前时间
4. 上架成功并在居民端展示

### 4.2 锁定库存流程
1. 居民在 Web 前端点击下单
2. 后端通过 Redis 或数据库乐观锁扣减库存
3. 生成订单及核销码

### 4.3 履约核销流程
1. 居民线下到店报出核销码
2. 商家在 Web 后台输入核销码校验
3. 订单状态变更为"已完成"
4. 居民账户增加低碳积分

---

## 5. 数据库设计

### 5.1 核心业务表

#### 5.1.1 biz_merchant - 商户信息表
```sql
CREATE TABLE biz_merchant (
    merchant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '关联sys_user',
    merchant_name VARCHAR(100) NOT NULL COMMENT '商户名称',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(255) COMMENT '详细地址',
    business_license VARCHAR(255) COMMENT '营业执照图片URL',
    license_status TINYINT DEFAULT 0 COMMENT '资质状态: 0-待审核 1-已通过 2-已拒绝',
    opening_hours VARCHAR(100) COMMENT '营业时间，如: 08:00-22:00',
    description TEXT COMMENT '店铺描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 5.1.2 biz_community - 社区信息表
```sql
CREATE TABLE biz_community (
    community_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    community_name VARCHAR(100) NOT NULL COMMENT '社区名称',
    community_code VARCHAR(50) NOT NULL COMMENT '社区编码',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    address VARCHAR(255) COMMENT '详细地址',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 5.1.3 biz_merchant_community - 商户-社区关联表
```sql
CREATE TABLE biz_merchant_community (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    community_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_merchant_community (merchant_id, community_id)
);
```

#### 5.1.4 biz_category - 食品分类表
```sql
CREATE TABLE biz_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 5.1.5 biz_product - 商品信息表
```sql
CREATE TABLE biz_product (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    category_id BIGINT COMMENT '分类ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    product_image VARCHAR(500) COMMENT '商品图片URL',
    original_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    discount_price DECIMAL(10,2) NOT NULL COMMENT '折扣价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    unit VARCHAR(20) DEFAULT '件' COMMENT '单位',
    expire_date DATE NOT NULL COMMENT '过期日期',
    expire_datetime DATETIME NOT NULL COMMENT '过期时间(精确到时分)',
    description TEXT COMMENT '商品描述',
    warning_hours INT DEFAULT 24 COMMENT '预警小时数(距离过期多久开始预警)',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架 1-上架 2-已售罄',
    create_by VARCHAR(64) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 5.1.6 biz_order - 订单表
```sql
CREATE TABLE biz_order (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '买家用户ID',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    verify_code VARCHAR(6) NOT NULL COMMENT '6位��销码',
    order_status TINYINT DEFAULT 0 COMMENT '订单状态: 0-待核销 1-已核销 2-已取消 3-已过期',
    carbon_saved DECIMAL(10,4) COMMENT '本次订单碳减排量(kg CO2)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    verify_time DATETIME COMMENT '核销时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_verify_code (verify_code)
);
```

#### 5.1.7 biz_user_profile - 用户资料扩展表
```sql
CREATE TABLE biz_user_profile (
    profile_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '关联sys_user',
    community_id BIGINT COMMENT '所属社区ID',
    carbon_points DECIMAL(10,2) DEFAULT 0 COMMENT '低碳积分',
    total_carbon_saved DECIMAL(10,4) DEFAULT 0 COMMENT '累计碳减排量(kg CO2)',
    total_food_saved DECIMAL(10,2) DEFAULT 0 COMMENT '累计挽救食品重量(kg)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 5.1.8 biz_carbon_log - 低碳日志表
```sql
CREATE TABLE biz_carbon_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_id BIGINT COMMENT '关联订单ID',
    carbon_points DECIMAL(10,2) NOT NULL COMMENT '本次获得积分',
    carbon_saved DECIMAL(10,4) NOT NULL COMMENT '本次碳减排量(kg)',
    log_type TINYINT NOT NULL COMMENT '日志类型: 1-订单获得 2-积分扣减',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### 5.1.9 biz_cart - 购物车表
```sql
CREATE TABLE biz_cart (
    cart_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id)
);
```

### 5.2 扩展RuoYi系统表

#### 5.2.1 扩展 sys_user 表
新增字段：
- user_type: TINYINT COMMENT '用户类型: 1-居民 2-商户 3-管理员'

---

## 6. API 接口设计

### 6.1 认证模块
- POST /api/auth/login - 用户登录
- POST /api/auth/register - 用户注册
- POST /api/auth/logout - 登出

### 6.2 居民端接口
- GET /api/consumer/products - 获取商品列表(按社区)
- GET /api/consumer/products/{id} - 商品详情
- POST /api/consumer/cart/add - 加入购物车
- GET /api/consumer/cart - 购物车列表
- DELETE /api/consumer/cart/{id} - 移除购物车商品
- POST /api/consumer/order/create - 创建订单
- GET /api/consumer/orders - 我的订单列表
- GET /api/consumer/order/{id} - 订单详情
- GET /api/consumer/carbon - 低碳中心(积分/碳减排)
- POST /api/consumer/community/bind - 绑定社区

### 6.3 商家端接口
- GET /api/merchant/profile - 商户资料
- PUT /api/merchant/profile - 更新商户资料
- POST /api/merchant/products - 添加商品
- PUT /api/merchant/products/{id} - 更新商品
- DELETE /api/merchant/products/{id} - 删除商品
- GET /api/merchant/products - 商品列表
- GET /api/merchant/products/warning - 预警商品列表
- POST /api/merchant/products/import - Excel批量导入
- GET /api/merchant/orders - 订单列表
- POST /api/merchant/order/verify - 核销订单
- GET /api/merchant/stats - 商家统计数据

### 6.4 管理端接口
- GET /api/admin/merchants - 商户列表
- PUT /api/admin/merchant/{id}/audit - 审核商户
- GET /api/admin/communities - 社区管理
- POST /api/admin/communities - 添加社区
- PUT /api/admin/communities/{id} - 更新社区
- GET /api/admin/categories - 分类管理
- POST /api/admin/categories - 添加分类
- GET /api/admin/dashboard/stats - 全局统计数据
- GET /api/admin/dashboard/chart - 图表数据

---

## 7. 菜单与权限设计

### 7.1 居民端菜单
- 首页/商品大厅
- 购物车
- 我的订单
- 低碳中心
- 个人中心

### 7.2 商家端菜单
- 首页/控制台(库存预警)
- 商品管理(列表/导入)
- 订单管理
- 核销工作台
- 店铺设置

### 7.3 管理端菜单
- 首页/数据大屏
- 系统管理(用户/角色/菜单/部门/字典)
- 商户管理
- 社区管理
- 商品分类
- 订单管理

---

## 8. Redis 缓存设计

### 8.1 库存缓存
- Key: `product:stock:{productId}`
- Value: 库存数量
- 用途: 高并发扣减库存

### 8.2 预警缓存
- Key: `product:warning:{merchantId}`
- Value: 预警商品列表
- 用途: 商家首页快速展示

### 8.3 验证码缓存
- Key: `verify:code:{orderId}`
- Value: 6位核销码
- TTL: 24小时

---

## 9. 碳计算公式

### 9.1 碳减排因子
根据食品类别定义碳排放因子(kg CO2/kg食品):
- 烘焙食品: 2.5
- 生鲜果蔬: 0.5
- 乳制品: 3.2
- 肉类: 7.0
- 饮料: 1.0
- 其他: 1.5

### 9.2 积分计算
- 低碳积分 = 碳减排量(kg) * 10 (取整)
- 每1kg碳减排 = 10积分

---

## 10. 项目目录结构

```
food-saving-platform/
├── backend/                    # 后端项目
│   ├── src/main/java/com/food/
│   │   ├── FoodSavingApplication.java
│   │   ├── config/             # 配置类
│   │   ├── controller/        # 控制器
│   │   ├── service/            # 业务服务
│   │   ├── mapper/             # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── security/           # 安全认证
│   │   └── common/             # 公共组件
│   ├── src/main/resources/
│   │   ├── mapper/             # MyBatis XML映射
│   │   └── application.yml
│   └── pom.xml
│
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API接口
│   │   ├── components/        # 公共组件
│   │   ├── views/             # 页面视图
│   │   │   ├── consumer/      # 居民端
│   │   │   ├── merchant/      # 商家端
│   │   │   └── admin/         # 管理端
│   │   ├── router/            # 路由配置
│   │   ├── store/             # 状态管理
│   │   └── utils/             # 工具函数
│   ├── package.json
│   └── vite.config.js
│
├── docs/                       # 文档
│   └── sql/                   # SQL脚本
│       └── init.sql
│
└── README.md
```

---

## 11. 验收标准

### 11.1 功能验收
- [ ] 居民可以注册、登录、绑定社区
- [ ] 居民可以浏览、搜索、加入购物车、下单
- [ ] 居民可以获得低碳积分和查看碳减排记录
- [ ] 商户可以注册、提交资质审核
- [ ] 商户可以发布、管理商品(单条/批量)
- [ ] 商户可以查看库存预警
- [ ] 商户可以核销订单
- [ ] 管理员可以审核商户、管理社区和分类
- [ ] 管理员可以查看数据大屏

### 11.2 性能验收
- [ ] 页面加载时间 < 3秒
- [ ] 库存扣减支持高并发
- [ ] 订单创建响应时间 < 500ms

### 11.3 安全验收
- [ ] 密码加密存储
- [ ] JWT Token认证
- [ ] 接口权限控制
- [ ] SQL注入防护

---

**文档版本**: v1.0
**创建日期**: 2026-03-15
