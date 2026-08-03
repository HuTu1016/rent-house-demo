package com.renthouse.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI rentHouseOpenApi() {
        return new OpenAPI().info(new Info().title("租房演示系统 API").version("v1").description("租客端与房东端业务闭环；支付仅支持线下报备与人工核销。"));
    }
}
