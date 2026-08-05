package com.renthouse.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI 3 接口文档统一配置类
 * <p>
 * 配置基础信息及统一 JWT 鉴权 Header，支持在 Knife4j 页面一键调试受保护接口。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI rentHouseOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("租房演示系统 API 文档")
                        .version("v1.0.0")
                        .description("基于 Knife4j 提供中介房源发布、租客浏览咨询、预约看房与即时通讯闭环接口文档。")
                        .contact(new Contact().name("RentHouse Dev Team")))
                // 配置统一的 JWT Bearer Token 认证头
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入有效登录 Token（无需手动拼 Bearer 前缀）")));
    }
}

