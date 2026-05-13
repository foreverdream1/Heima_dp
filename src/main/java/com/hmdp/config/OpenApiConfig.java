package com.hmdp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置类
 * 配置API文档信息，访问地址：http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("黑马点评 API 文档")
                        .description("黑马点评项目接口文档，基于Spring Boot 3 + SpringDoc OpenAPI")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("黑马程序员")
                                .url("https://www.itheima.com")
                                .email("contact@itheima.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}