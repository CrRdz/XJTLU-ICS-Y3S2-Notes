# CPT202Program

高校**毕业设计选题管理系统** — Spring Boot 3 全栈项目，支持学生、教师、管理员三种角色，涵盖选题发布、申请审核、标签分类、双因素认证、数据分析等完整业务流程。

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端语言 | Java 17 |
| 后端框架 | Spring Boot 3.2.4（MVC / Data JPA / Mail / Validation） |
| 数据库 | H2（开发/测试），MySQL 8.4（生产） |
| 缓存 | Redis（String 序列化，Jackson JSON） |
| 认证 | JWT（jjwt 0.12.5，HMAC-SHA）+ TOTP 双因素认证 |
| 密码 | SHA-256 → 计划迁移 BCrypt |
| 邮件 | Spring Mail（QQ SMTP, SSL 465） |
| 前端 | Vue 3 + TypeScript + Vite + Axios + ECharts |
| API 文档 | Knife4j 4.4（OpenAPI 3） |
| 工具 | Lombok, MapStruct（规划中）, VoConverter（泛型工具） |
| 测试 | JUnit 5 · Mockito · Spring MockMvc · H2 · JaCoCo |
| 构建 | Maven 3 |
| 容器化 | Docker + Docker Compose + Nginx 反向代理 |
| CI/CD | GitHub Actions（ci.yml + deploy.yml） |

---

## 项目结构

```
CPT202Program/
├── src/main/java/com/cpt202/
│   ├── CPT202Application.java          # Spring Boot 入口
│   ├── config/                         # WebMvc / Redis / CORS 配置
│   ├── constant/                       # MessageConstants, RedisKeyConstants, SecurityConstants
│   ├── context/                        # BaseContext（ThreadLocal 用户上下文）
│   ├── controller/
│   │   ├── common/    CommonAuthController        # 注册 · 登录 · OTP · 密码重置
│   │   ├── admin/     AdminUserController         # 用户管理（列表/状态/信息修改）
│   │   │              AdminCategoryController     # 分类 CRUD
│   │   │              AdminTagController          # 标签 CRUD
│   │   │              AdminRecordController       # 全量记录查询
│   │   ├── student/   StudentProfileController    # 个人资料 · 密码 · 2FA
│   │   │              StudentProjectController    # 浏览/搜索项目
│   │   │              StudentProjectRequestController  # 提交/撤回申请
│   │   │              StudentRequestHistoryController  # 申请历史
│   │   └── teacher/   TeacherProfileController    # 个人资料 · 密码 · 2FA
│   │                  TeacherProjectController    # 项目 CRUD · 状态变更
│   │                  TeacherProjectRequestController  # 审核申请
│   │                  TeacherProjectTagController      # 绑定项目标签
│   │                  TeacherAnalyticsController       # 数据看板
│   ├── dto/                            # 请求 DTO（~20 个）
│   ├── vo/                             # 响应 VO（~12 个）
│   ├── exception/                      # BusinessException, RuleViolationException, NotFoundException, UnauthorizedAccessException
│   ├── handler/                        # GlobalExceptionHandler
│   ├── interceptor/                    # JwtTokenInterceptor（URL 前缀 → 角色映射鉴权）
│   ├── model/entity/                   # JPA 实体（11 个）
│   ├── properties/                     # JwtProperties 配置类
│   ├── repository/                     # Spring Data JPA 仓库（10 个）
│   ├── result/                         # Result<T>, PageResult<T> 统一响应
│   ├── security/                       # AuthContext（JWT 解析结果记录）
│   ├── service/                        # 业务接口（18 个）
│   │   └── impl/                       # 业务实现（18 个，含泛型辅助方法）
│   ├── util/                           # PasswordUtil, VoConverter（泛型 Entity→VO 工具）
│   └── validation/                     # 领域校验层（解耦自 Service）
│       └── impl/
├── src/main/resources/
│   ├── application.properties          # 主配置
│   ├── data.sql                        # H2 开发种子数据
│   └── static/                         # 前端构建产物（Nginx 回源 / 后端直供）
├── src/test/java/com/cpt202/
│   ├── unit/                           # Mockito 单元测试（Service / Validation / Util）
│   ├── integration/                    # SpringBoot + MockMvc 集成测试
│   │   └── Cpt202IntegrationTest.java  # 集成测试基类（test profile, H2, 内存 Redis）
│   └── acceptance/                     # 端到端验收测试（全链路）
├── frontend/                           # Vue 3 + TypeScript 前端（独立 pnpm 项目）
│   ├── src/
│   │   ├── components/                 # 可复用组件
│   │   ├── composables/                # 组合式函数（useAuth, useRequest 等）
│   │   ├── layouts/                    # 布局组件
│   │   ├── router/                     # Vue Router 路由配置
│   │   ├── utils/                      # Axios 封装、工具函数
│   │   └── views/                      # 页面视图
│   └── vite.config.ts
├── nginx/
│   ├── nginx.conf                      # 反向代理配置（API → app:8080）
│   └── certs/                          # SSL 证书
├── scripts/
│   └── sync-frontend-static.sh         # 前端 dist → 后端 static 同步脚本
├── wiki/                               # 项目 Wiki 文档
├── docker-compose.yml                  # 生产编排（MySQL + App + Nginx）
├── docker-compose.dev.yml             # 开发覆盖（端口映射、独立网络）
├── Dockerfile                          # 多阶段构建（eclipse-temurin:17-jre-alpine）
└── pom.xml
```

---

## 架构分层

```
┌─────────────────────────────────────────────┐
│                  Nginx :80                  │  ← 反向代理 / 静态资源缓存
├─────────────────────────────────────────────┤
│              Controller 层                   │  ← @RestController, 按角色分包
│     common/   admin/   student/   teacher/  │
├─────────────────────────────────────────────┤
│         Interceptor (JwtTokenInterceptor)    │  ← URL 前缀 → 角色映射, 401 拦截
├─────────────────────────────────────────────┤
│               Service 层                     │  ← 业务接口 + 泛型辅助方法
│         (interface + impl, 18 对)            │
├──────────────────┬──────────────────────────┤
│   Validation 层   │     Util 层              │
│  (领域约束校验)    │  (PasswordUtil,         │
│                   │   VoConverter 泛型工具)   │
├──────────────────┴──────────────────────────┤
│             Repository 层                    │  ← Spring Data JPA (10 个)
├─────────────────────────────────────────────┤
│            Entity / Model 层                 │  ← JPA 实体 + DTO + VO
├─────────────────────────────────────────────┤
│         MySQL 8.4 / H2  +  Redis            │  ← 数据持久化 + 缓存
└─────────────────────────────────────────────┘
```

### 泛型化设计

项目在以下层面使用泛型消除重复代码：

| 位置 | 泛型方法 | 消除重复 |
|------|---------|---------|
| `VoConverter.toList()` | `<E, V> List<V> toList(List<E>, Function<E,V>)` | 5 处 `toXxxVOList()` 样板 |
| `ProfileServiceImpl.requireProfile()` | `<P> P requireProfile(id, repoFinder, msg, userExtractor, role)` | 6 处 findById+校验 |
| `ProfileValidationService.checkUserRole()` | `checkUserRole(User, UserRole)` | 按需替代 3 个角色校验方法 |
| `AuthServiceImpl.createRoleProfile()` | `<P> P createRoleProfile(dto, factory, repo, user, now)` | 注册时 Student/Teacher 档案创建 |

---

## 快速开始

### 本地开发（H2 内存数据库）

```bash
# 1. 确保 Java 17 + Maven 3 可用
java -version   # 17.x
mvn -version    # 3.x

# 2. 编译 + 测试
mvn test

# 3. 启动后端（H2 MySQL 兼容模式）
JDK17_BIN="/path/to/jdk-17/Contents/Home/bin"
export JAVA_HOME="${JDK17_BIN%/bin}"
export PATH="$JAVA_HOME/bin:$PATH"
export SPRING_DATASOURCE_URL="jdbc:h2:mem:cpt202db;MODE=MySQL;DB_CLOSE_DELAY=-1"
mvn spring-boot:run

# 4. 启动前端（另一个终端）
cd frontend
npm install
npm run dev
```

后端默认 `http://localhost:8080`，前端开发服务器 `http://localhost:5173`（代理 API 到后端）。

### Docker 部署（MySQL + Nginx）

```bash
# 构建镜像
mvn package -DskipTests
docker build -t cpt202program:latest .

# 启动全部服务
docker compose up -d

# 开发模式（独立端口/网络）
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

---

## 认证体系

### JWT + 角色路由鉴权

```
/api/admin/**   → 要求 ADMIN 角色
/api/teacher/** → 要求 TEACHER 角色
/api/student/** → 要求 STUDENT 角色
/api/common/**  → 公开访问
```

`JwtTokenInterceptor` 从 URL 前缀推断所需角色，解析 Bearer Token 后比对 `AuthContext.role()`，不匹配返回 401。

### 登录方式

| 方式 | 接口 |
|------|------|
| 用户名 + 密码 | `POST /api/common/auth/login` |
| 邮箱验证码 | `POST /api/common/auth/login/email-otp`（先 `send` 再登录） |
| 密码 + TOTP 双因素 | `POST /api/common/auth/login` → `POST /api/common/auth/login/2fa/verify` |

### 安全特性

- 密码 SHA-256 哈希（计划迁移 BCrypt）
- JWT HMAC-SHA 签名，可配置密钥与过期时间（默认无硬编码值）
- TOTP 双因素认证（RFC 6238, SHA-1, 30s 窗口, 6 位数字）
- 邮箱 OTP 验证码（5 分钟过期，60 秒冷却，Redis 存储）
- 密码重置令牌（30 分钟过期，一次性使用）
- `UserAuthStateService` 管理认证状态缓存与驱逐

---

## 测试

```
src/test/java/com/cpt202/
├── unit/         # Mockito 单元测试（不启动 Spring Context）
│   ├── service/impl/   AuthServiceImplTest, ProfileServiceImplTest, ...
│   ├── validation/impl/  ProfileValidationServiceImplTest, AuthValidationServiceImplTest
│   └── util/           PasswordUtilTest
├── integration/  # @SpringBootTest + MockMvc（test profile, H2, 内存 Redis）
│   ├── Cpt202IntegrationTest.java    # 基类
│   ├── CommonAuthControllerIntegrationTest
│   ├── AdminControllerIntegrationTest
│   ├── StudentControllerIntegrationTest
│   ├── TeacherControllerIntegrationTest
│   └── ProjectLifecycleIntegrationTest
└── acceptance/   # 全链路验收测试
    ├── CommonAuthAcceptanceTest
    ├── AdminGovernanceAcceptanceTest
    ├── SelfServiceProfileAcceptanceTest
    └── ProjectSelectionAcceptanceTest
```

```bash
mvn test                    # 全部 176 个测试
mvn test -Dtest="com.cpt202.unit.**"    # 仅单元测试
mvn test -Dtest="com.cpt202.integration.**"  # 仅集成测试
```

覆盖率报告：`target/site/jacoco/index.html`

---

## 核心业务规则

### 选题流程（Module 8）

```
学生浏览项目 → 提交申请(PENDING)
  → 教师审核 → ACCEPTED（通过）/ REJECTED（驳回）
       ↓
  通过后该学生其他 PENDING 申请自动 REJECTED
  项目录取满额后自动 CLOSED
```

- 每个教师项目独立设置申请截止日期
- 一名学生最多持有一个 PENDING 或 ACCEPTED 申请
- 学生可撤回 PENDING 申请（→ WITHDRAWN）
- 项目状态机：`AVAILABLE → REQUESTED → AGREED → CLOSED/ARCHIVED`

### 缓存策略

| 缓存键 | TTL | 内容 |
|--------|-----|------|
| `category_list` | 30 min | 分类全量列表 |
| `tag_list` | 30 min | 标签全量列表 |
| `email_login_otp:{email}` | 5 min | 邮箱登录验证码 |
| `email_register_otp:{email}` | 5 min | 邮箱注册验证码 |
| `password_reset:{token}` | 30 min | 密码重置令牌 |
| `2fa_setup:{userId}` | 10 min | TOTP 初始化密钥 |
| `auth_state:{userId}` | — | 用户认证状态 |

---

## API 文档

完整接口文档见 [`API_DOCUMENTATION.md`](API_DOCUMENTATION.md)。

后端规范文档见 [`wiki/backend-rules-specification.md`](wiki/backend-rules-specification.md)。

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [Wiki Home](wiki/Home.md) | 技术文档中心 |
| [Project Guide](wiki/Project-Guide.md) | 需求规格与产品待办 |
| [Code Refactoring Report](wiki/Code-Refactoring-Report.md) | 重构审计与路线图 |
| [Frontend Debugging Guide](wiki/Frontend-Debugging-Guide.md) | 前端调试教程 |
| [API Documentation](API_DOCUMENTATION.md) | REST API 详细说明 |
| [struct.md](struct.md) | 完整目录结构清单 |

---

> **维护者**：dengpingsong · **分支**：develop · **最后更新**：2026-05-10

---

## 快速启动

### 前置条件

- Java 17+
- Maven 3.9+

### 本地运行

```bash
mvn spring-boot:run
```

服务启动后可通过 **http://localhost:8080** 直接访问应用；部署环境通过 **Nginx 80 端口** 对外提供入口。

### API 文档

启动后访问 **http://localhost:8080/doc.html**（Knife4j UI）。

### 运行测试

```bash
mvn test
```

---

## CI/CD 流水线

GitHub Actions（`.github/workflows/ci.yml`）在每次推送或 PR 至 `main` / `master` / `develop` 时触发：

1. **Build** — Maven 编译项目
2. **Test** — 执行全部 JUnit 5 测试，上传 Surefire 报告
3. **Package** *(仅 main/master)* — 打包可执行 JAR 并上传为构建产物

生产/开发部署工作流（`.github/workflows/cd.yml`、`.github/workflows/devcd.yml`）会将以下文件一并上传到服务器并执行 `docker compose up -d`：

- `docker-compose.yml`
- `docker-compose.dev.yml`
- `nginx/nginx.conf`

其中 `nginx` 容器监听宿主机 `80/443` 端口，并将请求反向代理到内部 `app:8080`。

当前 `nginx` 路由策略：

- `/api/*`：转发到 Spring Boot 接口，并关闭浏览器缓存
- `*.css`、`*.js`：短时缓存（1 小时）
- 图片、字体等静态资源：较长缓存（7 天）
- `*.html` 和其他页面入口：不缓存，避免页面发布后客户端拿到旧页面

由于当前没有正式域名，CD 会在服务器首次部署时自动生成**自签名证书**以启用 HTTPS。因此技术上可以通过 `https://服务器IP或主机地址` 访问，但浏览器会提示证书不受信任；拿到域名后建议替换为受信任 CA 证书。
