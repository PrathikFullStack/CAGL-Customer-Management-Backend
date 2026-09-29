package com.iexceed.appzillonbanking.cagl.cm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CAGL Customer Management Microservice API")
                        .description("RESTful APIs for Grameen Maitri Existing Customer Management (CM) lifecycle - 360° Profile, Dashboard Queues, Section Updates, Workflow Transitions, and Record Locks.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CAGL & i-exceed Digital Banking Team")
                                .email("support@cagrameen.in"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.cagrameen.org/")))
                .servers(List.of(
                        new Server().url("/appzillonbankingcm").description("Current Context Server"),
                        new Server().url("http://localhost:9296/appzillonbankingcm").description("Local Development Server")
                ));
    }
}
