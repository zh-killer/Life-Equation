# 湖南省三甲医院 · 患者服务平台 - 后端

面向单家医院（湖南省三甲医院）的智慧医疗服务平台后端，提供在线挂号、AI 导诊、健康档案、报告解读、医患问答等能力。

## 技术栈

| 层次 | 技术 |
|---|---|
| 框架 | Spring Boot 3.2.5 / Java 18 |
| 持久层 | MyBatis-Plus 3.5.6 + MySQL 8.0 |
| 缓存 | Redis（Token 黑名单） |
| 安全 | Spring Security 6 + JWT（无状态）+ BCrypt + 方法级 RBAC（JWT 密钥强制 ≥32 字节） |
| 文档 | Knife4j（启动后访问 `/doc.html`） |
| AI | DeepSeek（文本对话 + 报告图片识别） |

## 主要模块

**患者端**：注册登录 / 微信登录、首页、科室与医生、排班查询、预约挂号（待支付 → 待确认 → 已确认 → 已完成，可取消并归还号源）、健康档案、就诊人管理、收藏、评价、用药提醒、消息通知。

**AI 能力**：
- AI 智能导诊（DeepSeek `deepseek-chat`）
- AI 健康建议
- AI 报告解读（DeepSeek `deepseek-flash` 多模态，直接识别化验单图片；识别失败时降级为手动输入数据 + 文本解读）

**医生端**：工作台、排班管理、预约处理、患者病历、咨询回复、评价查看。

**管理端**：数据看板、本院信息、医生、文章、疾病、药品、药房、预约、咨询、用户与角色管理。

## 快速开始

### 1. 准备数据库

```bash
mysql -uroot -p life_equation < init.sql
```

已有数据的老库升级时，按需执行：

| 脚本 | 作用 |
|---|---|
| `single_hospital_migration.sql` | 把多医院数据收窄为单家医院 |
| `doctor_fee_update.sql` | 按职称重定挂号费（12~50 元） |
| `schedule_rollout.sql` | 生成未来 30 天排班（幂等，可重复执行） |

> ⚠️ 本机若同时装了多个 MySQL，注意命令行默认端口可能与 Spring Boot 连接的不是同一个，执行前先 `SELECT @@port;` 确认。

### 2. 配置密钥

密钥不写在仓库里。把 `config/application-local.yml.example` 复制为 `config/application-local.yml`，填入自己的 Key：

```yaml
jwt:
  secret: 换成至少32字节的随机串   # 必填：缺失或过短，后端启动直接失败

deepseek:
  api:
    key: sk-你的deepseek-key   # 文本 + 图片识别共用

gaode:
  map:
    ak: 你的高德Key

wechat:
  miniapp:
    secret: 你的小程序secret
```

`application.yml` 通过 `spring.config.import` 自动加载该文件，也支持改用环境变量（`JWT_SECRET` / `DEEPSEEK_API_KEY` / `GAODE_AK` / `WECHAT_SECRET`）。

> ⚠️ `jwt.secret` 没有默认值：必须在上面两种方式之一里提供，且**不少于 32 字节**，否则后端启动即抛 `IllegalStateException`。

### 3. 启动

```bash
mvn spring-boot:run
```

服务默认监听 `8888`，接口文档 `http://localhost:8888/doc.html`。

检查 AI 服务是否真的通了：

```bash
# 登录后带上 token
curl -H "Authorization: Bearer <token>" http://localhost:8888/api/ai/report/diagnose
```

该接口会现场生成一张带检验数值的小图发给模型，能读对才返回 `status: OK`。

## Docker 部署

```bash
cp .env.example .env   # 填入 API Key
docker compose up -d
```

包含 MySQL、Redis、后端、前端四个服务，前端默认监听 `80`。

## 目录结构

```
src/main/java/com/lifeequation/
├── common/        统一返回体、异常处理
├── config/        Security / Knife4j / Web 配置
├── controller/    REST 接口
├── entity/        实体类
├── mapper/        MyBatis-Plus Mapper
├── security/      JWT 过滤器、SecurityUtils
└── service/       业务逻辑（impl 为实现）
```

## 安全说明

- **JWT 无默认密钥**：`jwt.secret` 只从环境变量或 `application-local.yml` 读取，缺失或不足 32 字节时启动即失败，避免用公开默认值离线伪造管理员令牌。
- **调试后门已移除**：`POST /api/auth/set-role` 已删除，角色变更只能走管理员接口。
- **越权防护**：AI 咨询 / 健康建议 / 报告解读的详情接口校验记录归属，非本人返回 403。
- **上传白名单**：报告图片只接受 `jpg/jpeg/png/gif/webp` 后缀。

## 说明

- 本项目为教学/毕业设计用途，AI 输出仅供参考，不构成医疗建议。
- 挂号支付为**模拟支付**，未对接任何真实支付网关。
