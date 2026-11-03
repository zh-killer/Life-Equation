# ============================================================
# 生命方程式 - 后端 Dockerfile (Spring Boot 3.2.5 + JDK 18)
# ⚠️ 实验性：配套 compose 未端到端验证，见 deploy/README.md
# 先本地执行 mvn clean package -DskipTests 再 docker compose up -d
# ============================================================

FROM eclipse-temurin:18-jre
WORKDIR /app

# 创建上传目录
RUN mkdir -p /app/uploads

# 直接复制本地构建好的 JAR
COPY target/life-equation-1.0.0.jar app.jar

EXPOSE 8080

# 激活 prod 配置
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
