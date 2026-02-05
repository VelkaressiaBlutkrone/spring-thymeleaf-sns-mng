package com.example.sns.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

/**
 * OpenAPI(Swagger) 설정.
 *
 * RULE 4.3(공개 API Swagger 필수). API 문서 골격 노출.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("지도 기반 SNS 웹/모바일 통합 플랫폼 API")
                        .description("REST API 명세. 인증: 세션·쿠키(Redis). 상세: doc/API_SPEC.md")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API 문서")
                                .url("/doc/API_SPEC.md")))
                .addServersItem(new Server()
                        .url("/")
                        .description("로컬 서버"));
    }
}
