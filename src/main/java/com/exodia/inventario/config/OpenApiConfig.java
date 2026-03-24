package com.exodia.inventario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventarioOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Exodia Inventario API")
                        .version("1.0")
                        .description("Microservicio de inventario del ERP Exodia"));
    }
}
