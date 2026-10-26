package com.lifeequation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (Swagger) API 文档配置
 * <p>
 * 访问地址：
 * <ul>
 *   <li>Knife4j UI: <a href="http://localhost:8888/doc.html">http://localhost:8888/doc.html</a></li>
 *   <li>OpenAPI JSON: <a href="http://localhost:8888/v3/api-docs">http://localhost:8888/v3/api-docs</a></li>
 * </ul>
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("湖南省三甲医院 - 患者服务平台 API")
                        .version("1.0.0")
                        .description("基于 Spring Boot + Vue 3 的医疗健康综合服务平台，"
                                + "集成 DeepSeek 大模型实现 AI 导诊、健康建议、报告解读等功能。"
                                + "<br><br>"
                                + "**认证方式**：除公开接口外，需在请求头携带 "
                                + "<code>Authorization: Bearer &lt;token&gt;</code>")
                        .contact(new Contact()
                                .name("开发者")
                                .email("your-email@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
