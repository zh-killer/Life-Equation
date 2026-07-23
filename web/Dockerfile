# ============================================================
# 生命方程式 - 前端 Dockerfile (Vue 3 + Vite → Nginx)
# 多阶段构建：Node 编译 → Nginx 托管
# ============================================================

# --- 阶段1: Node 构建 ---
FROM node:22-alpine AS builder
WORKDIR /build
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- 阶段2: Nginx 托管 ---
FROM nginx:1.27-alpine
# 复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html
# 复制 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
