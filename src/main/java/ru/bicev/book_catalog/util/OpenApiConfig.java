package ru.bicev.book_catalog.util;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Book catalogue API")
                        .description("API for managing users, authors and books")
                        .version("1.0.0"));
    }

    @Bean(name = "pageableOperationCustomizer")
    public OperationCustomizer pageableCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getParameters() != null) {
                for (Parameter parameter : operation.getParameters()) {
                    switch (parameter.getName()) {
                        case "page" -> {
                            parameter.setDescription("Page number (begins with 0)");
                            parameter.setExample(0);
                        }
                        case "size" -> {
                            parameter.setDescription("Number of elements per page");
                            parameter.setExample(10);
                        }
                        case "sort" -> {
                            parameter.setDescription(
                                    "Sorting format: field,direction (example id,asc or title,desc)");
                            parameter.setExample("id,asc");
                        }
                    }
                }
            }
            return operation;
        };

    }

}
