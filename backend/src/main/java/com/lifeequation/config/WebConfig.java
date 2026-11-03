package com.lifeequation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web MVC configuration.
 * Interceptor registration has been moved to SecurityConfig (Spring Security).
 * This class now only handles static resource mapping for file uploads.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** 与 application.yml 的 file.upload.path 保持一致；这里给默认值，避免配置缺失时整容器启动失败 */
    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 相对路径必须转成绝对路径，否则 Spring 会当成 classpath 资源去找
        String location = Paths.get(uploadPath).toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(location);
    }
}
