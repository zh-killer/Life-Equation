# 容器化部署（实验性，未端到端验证）

> 结论先行：`backend/docker-compose.yml` + `backend/Dockerfile` **没有在干净环境里完整跑通过**。
> 现在直接 `docker compose up` 会在第 1～2 步就失败。这份文档记录已知缺口和补法，
> 补完并真跑通之后，再回来把「实验性」这几个字删掉。

## 已知缺口

| # | 位置 | 问题 | 补法 |
|---|---|---|---|
| 1 | `docker-compose.yml` frontend.build.context | 写的是 `../life_eqation`，仓库里这个目录早就改名成 `web/` | 已改成 `../web`；仍未验证镜像内 dist 路径 |
| 2 | `docker-compose.yml` backend.environment | 传了 `SPRING_PROFILES_ACTIVE=prod`，但仓库里没有 `application-prod.yml`（根 `.gitignore` 还把它忽略了） | 二选一：补一份 prod 配置；或删掉这行，用默认配置 + 环境变量覆盖 |
| 3 | `backend/src/main/resources/application.yml` datasource / redis | 容器里 `localhost:3306` 指容器自己，连不到 MySQL | 改成 `${MYSQL_HOST:localhost}` / `${REDIS_HOST:localhost}`，compose 里传 `MYSQL_HOST=mysql`、`REDIS_HOST=redis` |
| 4 | 根 `.env.example` vs `backend/.env` | README 让人在根目录 `cp .env.example .env`，但 compose 在 `backend/` 下，只读 `backend/.env` | 统一到一处：要么 compose 加 `--env-file ../.env`，要么文档改成在 `backend/` 里放 `.env` |
| 5 | 端口 | 后端 `server.port: 8888`、`EXPOSE 8080`、`nginx.conf` 反代 `backend:8080` 三者不一致 | 统一成 8080：yml 里 `server.port: ${SERVER_PORT:8080}`，或 compose 里传 `SERVER_PORT=8080` |

`JWT_SECRET` 用的是 `${JWT_SECRET:?...}`，`.env` 没准备好会直接让 compose 退出——这是有意为之，别改成默认值。

## 补完之后怎么验

```bash
# 1. 先确认本地能构建（这一步和容器无关，但必须先过）
cd backend && mvn clean package -DskipTests && cd ..
cd web && npm install && npm run build && cd ..

# 2. 准备 compose 读得到的 env
cd backend
cp .env.example .env      # 填 JWT_SECRET（≥32 字节）、MYSQL_PASSWORD、DEEPSEEK_API_KEY

# 3. 起
docker compose up -d --build
docker compose ps         # 四个服务都应是 healthy / running
docker compose logs -f backend | head -50

# 4. 验收
curl -s http://localhost:8080/api/home | head -c 200     # 后端通
curl -sI http://localhost | head -3                      # Nginx 通
# 浏览器打开 http://localhost ，用 init.sql 里的账号登录，点一次头像上传
```

第 4 步的上传验证别省：文件落盘 + `/uploads/**` 静态映射是容器卷最容易出错的地方。

## 当前能确认的部分

- `backend/Dockerfile` 采用「本地先 `mvn package`，镜像只拷贝 jar」的方式，构建快、不依赖镜像里的 Maven。
- `uploads-data` 卷挂到 `/app/uploads`，和后端默认上传目录（工作目录下的 `uploads/`）是对得上的。
- MySQL 用 `init.sql` 初始化，Redis 带密码启动，两者都配了 healthcheck。

其余（Nginx 反代、profile、服务名解析）需要真跑一遍才有结论。
