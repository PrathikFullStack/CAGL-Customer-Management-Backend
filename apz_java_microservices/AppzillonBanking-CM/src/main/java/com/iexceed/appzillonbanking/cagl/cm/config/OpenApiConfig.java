package com.iexceed.appzillonbanking.cagl.cm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customerManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAGL Maitri Customer Management Microservice API")
                        .description("RESTful APIs for Customer 360° Profile, KM Workflow Queues, Section Updates, Concurrency Locks, and CDH Ingestion.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CAGL Digital Platform Team")
                                .email("support@cagl.com")));
    }
}
