# Life Equation 生命方程式

基于 Spring Boot + Vue3 + 微信小程序 的全栈健康管理平台。

## 📁 项目结构

```
-LIfe-Equation/
├── backend/    # Spring Boot 后端 API
├── web/        # Vue3 Web 管理端
└── mini/       # 微信小程序端
```

## 🏗️ 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot + Spring Security + MyBatis-Plus |
| 数据库 | MySQL + Redis |
| Web 前端 | Vue 3 + Vite + Element Plus |
| 小程序 | Uni-app |
| 部署 | Docker + Nginx |

## 🚀 快速启动

### 后端
```bash
cd backend
mvn spring-boot:run
```

### Web 前端
```bash
cd web
npm install
npm run dev
```

### 小程序
```bash
cd mini
npm install
npm run dev:mp-weixin
```
