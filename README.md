# 🏥 生命方程式 (Life Equation)

> 湖南省三甲医院 · 患者服务平台

生命方程式是一个集**健康管理**、**智慧医疗**、**AI 辅助诊疗**于一体的全栈应用，面向**单家医院（湖南省三甲医院）**提供在线服务。提供 Web 端 + 微信小程序端，覆盖患者就医全流程：从 AI 智能导诊、预约挂号、在线咨询、药品查询，到医生排班、患者管理、管理后台数据统计。

---

## ✨ 核心功能

### 🤖 AI 智能模块
| 功能 | 说明 |
|------|------|
| **AI 智能导诊** | 输入症状，AI 辅助判断可能的疾病方向并推荐就诊科室 |
| **AI 健康建议** | 根据个人健康情况生成个性化养生、饮食、运动建议 |
| **AI 报告解读** | 上传体检报告/化验单图片，DeepSeek 多模态模型直接识别图中数据并解读指标含义（识别失败可降级为手动输入数据） |

### 👤 患者端（Web + 小程序）
| 功能 | 说明 |
|------|------|
| 账号系统 | 注册/登录（支持微信小程序授权登录） |
| 本院概况 | 查看本院简介、科室、地址与地图（高德地图集成） |
| 医生预约 | 按科室/医院筛选医生，查看排班并在线预约 |
| 在线咨询 | 提交医疗问题，医生在线回复（类似问诊社区） |
| 疾病百科 | 浏览常见疾病症状、病因、治疗建议 |
| 药品查询 | 药品详情、用法用量、注意事项 |
| 院内药房 | 查看本院门诊药房、急诊药房信息 |
| 健康档案 | 个人病历、就诊记录管理 |
| 用药提醒 | 设置药品服用定时提醒 |
| 家人管理 | 为家人创建健康档案 |
| 健康资讯 | 阅读健康科普文章 |
| 收藏功能 | 收藏感兴趣的医生、文章、疾病 |

### 🩺 医生端（Web + 小程序）
| 功能 | 说明 |
|------|------|
| 工作台 | 今日预约、待回复咨询、患者统计一览 |
| 排班管理 | 设置出诊日期与时段 |
| 患者管理 | 查看就诊患者列表与病历 |
| 咨询回复 | 在线回答患者提问 |
| 评价查看 | 查看患者对服务的评分和评价 |

### 🛠 管理员端（Web）
| 功能 | 说明 |
|------|------|
| 数据仪表盘 | 用户数、预约数、咨询数等核心指标可视化（ECharts） |
| 本院信息 | 维护本院基本资料（单院模式，不提供新建/删除） |
| 医生管理 | 管理医生信息、所在医院、科室 |
| 疾病管理 | 维护疾病百科数据 |
| 药品管理 | 维护药品信息数据 |
| 药店管理 | 维护药店信息 |
| 文章管理 | 发布/编辑健康资讯文章 |
| 用户管理 | 查看用户列表、分配角色权限 |

---

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────┐
│                   客户端层                        │
│  ┌──────────────┐  ┌──────────────────────────┐ │
│  │ 微信小程序     │  │  Web 管理端 (Vue 3)       │ │
│  │ (Uni-app)    │  │  Element Plus + ECharts  │ │
│  └──────┬───────┘  └────────────┬─────────────┘ │
└─────────┼──────────────────────┼────────────────┘
          │       HTTPS/REST      │
          ▼                       ▼
┌─────────────────────────────────────────────────┐
│                 Spring Boot 3.2                  │
│  ┌──────────┬──────────┬──────────────────────┐ │
│  │ 安全层    │  业务层   │  AI 集成               │ │
│  │ JWT+RBAC │  预约/咨询 │  DeepSeek(文本+视觉)   │ │
│  └──────────┴──────────┴──────────────────────┘ │
└───────┬──────────────┬──────────────────────────┘
        ▼              ▼
   ┌─────────┐   ┌─────────┐
   │  MySQL  │   │  Redis  │
   └─────────┘   └─────────┘
```

| 层级 | 技术选型 |
|------|---------|
| **后端框架** | Spring Boot 3.2.5 (Java 18) |
| **ORM** | MyBatis-Plus 3.5.6 |
| **安全** | Spring Security + JWT + RBAC 角色权限 |
| **数据库** | MySQL 8.0 |
| **缓存** | Redis 7 |
| **AI 大模型** | DeepSeek：`deepseek-chat` 文本对话 + `deepseek-flash` 多模态图片识别 |
| **地图服务** | 高德地图 API（地理编码） |
| **Web 前端** | Vue 3 + Vite + Element Plus + ECharts + Axios |
| **小程序** | Uni-app (Vue 3) → 微信小程序 |
| **部署** | Docker Compose + Nginx |
| **API 文档** | Knife4j (Swagger) |

---

## 📁 项目结构

```
-Life-Equation/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/lifeequation/
│   │   ├── common/                   # 通用工具（JWT、全局异常处理等）
│   │   ├── config/                   # Spring 配置（Security、MyBatis、跨域等）
│   │   ├── controller/               # 控制器（32 个 Controller）
│   │   │   ├── Admin*Controller      # 管理员端 API
│   │   │   ├── Ai*Controller         # AI 智能模块 API
│   │   │   ├── Doctor*Controller     # 医生端 API
│   │   │   └── ...                   # 患者端 API
│   │   ├── entity/                   # 数据库实体（22 个 Entity）
│   │   ├── mapper/                   # MyBatis Mapper
│   │   └── service/                  # 业务服务（23 个 Service）
│   │       ├── *Ai*Service           # AI 大模型集成
│   │       ├── WeChatService         # 微信登录
│   │       └── BaiduMapService       # 地图服务
│   ├── src/main/resources/
│   │   ├── application.yml           # 开发环境配置（密钥通过 config/application-local.yml 注入）
│   │   └── application-prod.yml      # 生产环境配置
│   ├── config/
│   │   └── application-local.yml.example  # 本地密钥模板（复制为 application-local.yml 后填写）
│   ├── init.sql                      # 数据库初始化脚本
│   ├── hospital_coords.sql           # 医院坐标数据
│   ├── single_hospital_migration.sql # 多医院 → 单院历史数据迁移
│   ├── doctor_fee_update.sql         # 按职称重定挂号费
│   ├── schedule_rollout.sql          # 生成未来 30 天排班（幂等）
│   ├── Dockerfile
│   ├── docker-compose.yml            # 一键启动 MySQL + Redis + 后端 + 前端
│   └── pom.xml
│
├── web/                              # Vue 3 Web 管理端
│   ├── src/
│   │   ├── views/
│   │   │   ├── admin/                # 管理员页面（8 个页面）
│   │   │   ├── doctor/               # 医生端页面（4 个页面）
│   │   │   └── ...                   # 公共/患者页面（22 个页面）
│   │   ├── router/index.js           # 路由配置（含角色权限守卫）
│   │   ├── layouts/                  # 布局组件
│   │   └── components/               # 通用组件
│   ├── Dockerfile
│   ├── nginx.conf                    # Nginx 配置
│   ├── vite.config.js
│   └── package.json
│
├── mini/                             # Uni-app 微信小程序
│   ├── src/pages/
│   │   ├── patient/                  # 患者端（20+ 个页面模块）
│   │   │   ├── ai/                   # AI 导诊 / 健康建议 / 报告解读
│   │   │   ├── appointment/          # 预约挂号
│   │   │   ├── consultation/         # 在线咨询
│   │   │   ├── health/               # 健康档案
│   │   │   ├── reminder/             # 用药提醒
│   │   │   └── ...
│   │   └── doctor/                   # 医生端（6 个页面模块）
│   ├── package.json
│   └── vite.config.js
│
├── .env.example                      # 环境变量模板（部署前复制为 .env 并填写）
├── .gitignore
└── README.md
```

---

## 🚀 快速启动

### 前置要求

- **JDK 18+** — 后端运行
- **Maven 3.8+** — 后端构建
- **Node.js 20+** — 前端/小程序开发
- **MySQL 8.0 + Redis 7** — 数据存储与缓存
- **微信开发者工具** — 小程序调试运行

### 方式一：Docker Compose 一键部署（推荐生产环境）

```bash
# 1. 克隆项目
git clone https://github.com/zh-killer/-Life-Equation.git
cd -Life-Equation

# 2. 配置环境变量
cp .env.example .env
# 编辑 .env 填写你的 API Key：DEEPSEEK_API_KEY 等

# 3. 构建后端 JAR
cd backend
mvn clean package -DskipTests
cd ..

# 4. 构建前端
cd web
npm install && npm run build
cd ..

# 5. 一键启动所有服务（MySQL + Redis + 后端 + 前端 Nginx）
cd backend
docker compose up -d

# 6. 访问
# Web 前端: http://localhost
# 后端 API: http://localhost:8080
# API 文档: http://localhost:8080/doc.html
```

### 方式二：本地开发启动

**启动后端：**

```bash
cd backend

# 确保 MySQL 和 Redis 已在本地运行

# 填密钥：把 config/application-local.yml.example 复制为 config/application-local.yml
# 修改 application.yml 中的数据库连接信息，或设置环境变量
# 然后启动
mvn spring-boot:run

# 后端运行在 http://localhost:8888
# API 文档: http://localhost:8888/doc.html
```

**启动 Web 前端：**

```bash
cd web
npm install
npm run dev

# 前端运行在 http://localhost:5173
```

**启动小程序：**

```bash
cd mini
npm install
npm run dev:mp-weixin

# 用微信开发者工具打开 mini/dist/dev/mp-weixin 目录
```

---

## 🔧 环境变量配置

复制 `.env.example` 为 `.env`，填写以下配置：

| 变量名 | 说明 | 必填 |
|--------|------|------|
| `MYSQL_USER` / `MYSQL_PASSWORD` | 数据库连接信息 | ✅ |
| `REDIS_PASSWORD` | Redis 密码 | ✅ |
| `JWT_SECRET` | JWT 签名密钥（自定义随机字符串） | ✅ |
| `DEEPSEEK_API_KEY` | DeepSeek API Key（从 [platform.deepseek.com](https://platform.deepseek.com) 获取） | AI 导诊 / 健康建议 / 报告解读都需要（文本和图片识别共用同一个 Key） |
| `GAODE_AK` | 高德地图 API Key（从 [lbs.amap.com](https://lbs.amap.com) 获取） | 地图功能需要 |
| `WECHAT_APPID` / `WECHAT_SECRET` | 微信小程序 AppID 和 Secret | 小程序登录需要 |

> 💡 **本地开发**：密钥不必写进 `application.yml`。把 `backend/config/application-local.yml.example` 复制为同目录下的 `application-local.yml` 并填入即可，该文件已在 `.gitignore` 中，不会被提交。
>
> 💡 **开发阶段**：即使不填 AI / 地图 / 微信的 Key，项目核心功能（本院浏览、预约、咨询等）也能正常运行，只是 AI 和地图相关功能会降级。
>
> 🔍 **自检**：登录后请求 `GET /api/ai/report/diagnose`，服务端会现场生成一张带检验数值的小图发给模型，能读对才返回 `status: OK`。

---

## 👥 用户角色

项目内置 **RBAC 权限模型**，三种角色：

| 角色 | 权限范围 |
|------|---------|
| **USER** (患者) | 浏览信息、预约挂号、在线咨询、AI 功能、健康档案、家人管理 |
| **DOCTOR** (医生) | 工作台、排班管理、患者管理、咨询回复、评价查看 |
| **ADMIN** (管理员) | 数据仪表盘、管理所有实体（医院/医生/疾病/药品/药店/文章/用户） |

权限通过 `role`、`permission`、`role_permission`、`user_role` 四张表控制，数据库初始化脚本 `init.sql` 中已预设基础数据。

---

## 📡 API 概览

启动后端后访问 `http://localhost:8888/doc.html` 查看 Knife4j 生成的完整 API 文档。

| 模块 | 端点前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth/**` | 登录、注册、微信授权、个人信息 |
| 首页 | `/api/home` | 首页聚合数据 |
| 本院 | `/api/hospitals/**` | 本院详情与列表 |
| 医生 | `/api/doctors/**` | 医生信息、排班查询 |
| 预约 | `/api/appointments/**` | 预约挂号、取消（`/{id}/cancel`）、模拟支付（`/{id}/pay`） |
| 咨询 | `/api/consultations/**` | 在线医疗咨询 |
| 疾病 | `/api/diseases/**` | 疾病百科 |
| 药品 | `/api/drugs/**` | 药品信息 |
| 药房 | `/api/pharmacies/**` | 院内药房 |
| 文章 | `/api/articles/**` | 健康资讯 |
| 健康档案 | `/api/health-records/**` | 个人病历管理 |
| 用药提醒 | `/api/medication-reminders/**` | 药品定时提醒 |
| 家人管理 | `/api/family-members/**` | 家人档案 |
| 收藏 | `/api/favorites/**` | 收藏功能 |
| 评价 | `/api/reviews/**` | 就诊评价 |
| AI 导诊 | `/api/ai/consultation/**` | AI 症状分析 |
| AI 健康建议 | `/api/ai/health-advice/**` | AI 健康指导 |
| AI 报告解读 | `/api/ai/report/**` | AI 化验单识别与解读 |
| 医生工作台 | `/api/doctor/**` | 医生端数据、排班、预约处理 |
| 管理员 | `/api/admin/**` | 仪表盘与全量管理 API |

> 预约状态流转：`UNPAID`（待支付）→ `PENDING`（待确认）→ `CONFIRMED`（已确认）→ `COMPLETED`（已完成）；未完成状态可取消，取消时会归还该时段号源。支付为**模拟支付**，未对接真实支付网关。

---

## 🗺️ 项目关系

```
┌──────────────────────┐
│   life_equation_mini  │  微信小程序 (Uni-app)
│      (mini/)         │  患者端 + 医生端
└─────────┬────────────┘
          │ 调用 REST API
          ▼
┌──────────────────────┐      ┌─────────┐
│   Quation of Life     │ ──── │  MySQL  │
│      (backend/)       │ ──── │  Redis  │
│   Spring Boot 后端    │      └─────────┘
└─────────┬────────────┘
          │ 同一套 API
          ▼
┌──────────────────────┐
│     life_eqation      │  Web 管理端 (Vue 3)
│       (web/)         │  患者 + 医生 + 管理员
└──────────────────────┘
```

---

## 📄 License

MIT License — 仅供学习交流使用。

---

> 🌟 如果这个项目对你有帮助，欢迎给个 Star！
