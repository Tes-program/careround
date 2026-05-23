package com.careround.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI careroundOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareRound API")
                        .version("1.0.0")
                        .description("API documentation for the CareRound digital ward management system.")
                        .contact(new Contact().name("CareRound")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    /** Adds standard error responses to every operation so the Responses section is populated. */
    @Bean
    public OperationCustomizer globalErrorResponses() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }
            responses.addApiResponse("400", new ApiResponse().description("Bad request — validation failed"));
            responses.addApiResponse("401", new ApiResponse().description("Unauthorized — JWT token missing or invalid"));
            responses.addApiResponse("403", new ApiResponse().description("Forbidden — insufficient role for this operation"));
            responses.addApiResponse("404", new ApiResponse().description("Resource not found"));
            responses.addApiResponse("500", new ApiResponse().description("Internal server error"));
            return operation;
        };
    }
}
