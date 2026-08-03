package com.renthouse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI rentHouseOpenApi() {
        return new OpenAPI().info(new Info().title("租房演示系统 API").version("v1").description("中介房源发布、租客浏览咨询和预约看房业务。"));
    }
}
