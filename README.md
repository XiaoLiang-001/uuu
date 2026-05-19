# 农家优品（NCP）

面向课程/毕设演示的 **农产品电商 Web 系统**：普通用户完成浏览、加购、下单与评价；管理员完成商品、用户及产地/分类字典等后台治理。系统可本地部署，**不接真实支付**，侧重交易主链路与数据一致性。

---

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 / 构建 | Java 8、Maven |
| 框架 | Spring Boot 2.4.5、Spring MVC、Spring Security |
| 持久层 | MyBatis-Plus、MySQL |
| 页面 | Thymeleaf（服务端渲染）；管理端部分列表使用 Vue + Axios |
| 缓存 | Spring Cache（默认 Redis，可关闭） |
| 其他 | Lombok、Druid（依赖已引入）、本地文件上传 |

---

## 功能概览

### 用户端

- 注册 / 登录（Session，表单登录）
- 农产品集市：浏览、检索、按产地/分类筛选
- 购物车：加购、改数量、移除；服务端校验库存与上架状态
- 订单：结算下单、订单列表与详情（事务内写单、扣库存）
- 商品评价：购后评论（资格与订单关联校验）
- 个人资料查看与修改

### 管理端

- 农产品维护：新增/编辑、上下架、批量操作、图片上传
- 产地与分类字典维护（含引用检查）
- 用户账号治理：查询、启用/禁用、新增与删除（受规则约束）
- 集市预览（与用户端展示对照）

---

## 项目结构（简要）

```
ncp/
├── src/main/java/com/itheima/ncp/
│   ├── controller/      # 页面与 REST API
│   ├── service/         # 业务逻辑
│   ├── mapper/          # MyBatis-Plus Mapper
│   ├── entity/          # 实体
│   ├── security/        # Spring Security、密码编码
│   ├── config/          # Web、缓存等配置
│   └── dto/             # 请求/响应 DTO
├── src/main/resources/
│   ├── templates/       # Thymeleaf 页面
│   ├── static/          # CSS、JS
│   ├── sql/             # 建表与初始化数据
│   └── application.yml  # 应用配置
├── uploads/             # 商品图片（运行后生成）
└── docs/                # 流程图等文档
```

---

## 环境要求

- **JDK 8+**
- **Maven 3.6+**
- **MySQL 5.7+ / 8.x**（建议 `utf8mb4`）
- **Redis**（可选；未安装时见下方「无 Redis」说明）

---

## 快速开始

### 1. 创建数据库并执行脚本

在 MySQL 中 **按顺序** 执行（路径：`src/main/resources/sql/`）：

| 顺序 | 文件 | 说明 |
|------|------|------|
| 1 | `schema-mysql.sql` | 建库 `ncp`、用户表 |
| 2 | `schema-agri-product.sql` | 商品、产地、分类等 |
| 3 | `schema-shop-mysql.sql` | 购物车、订单、评论 |
| 4 | `data-mysql.sql` | 演示账号 |
| 5 | `data-agri-product.sql` | 演示商品与字典数据 |

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

- `spring.datasource.url`：库地址与端口（示例为 `localhost:3307/ncp`）
- `spring.datasource.username` / `password`：数据库账号
- `spring.redis.host` / `port`：Redis 地址（若使用缓存）

> 请勿将含真实口令的配置提交到公开仓库。

### 3. 无 Redis 时

将缓存改为关闭，避免启动依赖 Redis：

```yaml
spring:
  cache:
    type: none
```

（并确保未强制连接 Redis 的其他配置；项目内 `config.cache` 包提供降级能力，以实际代码为准。）

### 4. 启动应用

```bash
mvn clean package -DskipTests
java -jar target/ncp-0.0.1-SNAPSHOT.jar
```

或在 IDE 中运行主类：`com.itheima.ncp.NcpApplication`。

默认访问：**http://localhost:8080**（未在配置中指定 `server.port` 时）。

### 5. 演示账号

执行 `data-mysql.sql` 后可用（密码为 BCrypt 存储，下表为明文）：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `demo_user` | `user123` | 普通用户 |
| `demo_admin` | `admin123` | 管理员 |
| `tester1` | `test123` | 普通用户 |

- 登录页：`/login`
- 注册页：`/register`
- 管理端入口：使用管理员账号登录后访问 `/admin/**`

---

## 主要访问路径

| 路径 | 说明 |
|------|------|
| `/login` | 登录 |
| `/register` | 注册 |
| `/user/home` | 用户端首页 |
| `/user/market` | 集市 |
| `/user/cart` | 购物车 |
| `/user/orders` | 订单 |
| `/admin/home` | 管理端首页 |
| `/api/auth/login` | JSON 登录（Session，无 JWT） |

受保护资源未登录时：页面跳转登录；`/api/**` 返回 JSON 401（见 `SecurityConfig`）。

---

## 配置说明

| 配置项 | 含义 |
|--------|------|
| `app.upload.dir` | 商品图片本地目录，默认 `uploads` |
| `spring.cache.*` | 缓存类型与 TTL；缓存名含 `marketList`、`productById` 等 |
| `spring.servlet.multipart.*` | 上传大小限制 |

图片访问一般通过应用内文件接口（如 `/files/**`），具体见 `ProductFileController`。

---

## 安全说明

- 认证：**Spring Security** + **HttpSession**
- 密码：`FlexiblePasswordEncoder` 支持 **BCrypt** 新密码与历史 **明文** 兼容校验
- 角色：`USER` / `ADMIN`，管理端接口需 `ROLE_ADMIN`
- 业务数据（购物车、订单、评论等）在服务端做 **归属与资格校验**

---

## 构建与测试

```bash
mvn test          # 单元测试（含 H2 等测试依赖）
mvn clean package # 打包
```

---

## 局限与说明

- 未接入真实 **支付、对账、物流** 履约，适用于教学演示与小规模访问。
- 性能与并发能力未按生产级压测设计；部署生产环境需另行加固（HTTPS、口令管理、日志监控等）。

---

## 许可证

本项目为课程/毕设演示用途；第三方组件遵循其各自开源协议。
