package ru.bookstore.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ProblemDetail;

@OpenAPIDefinition(info = @Info(title = "Bookstore Api", description = "API книжного магазина",
        version = "1.0.0", contact = @Contact(name = "Nemkov Daniil", url = "https://t.me/nemk0ff")))
@SecurityScheme(name = "JWT", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new io.swagger.v3.oas.models.info.Info().title("API Documentation").version("1.0"))
        .components(new Components()
            .addSchemas("ProblemDetail", new Schema<ProblemDetail>()
                .type("object")
                .properties(
                    Map.of(
                        "title", new StringSchema().description("Тип ошибки"),
                        "status", new IntegerSchema().description("HTTP статус код"),
                        "detail", new StringSchema().description("Детальное описание ошибки"),
                        "timestamp", new StringSchema().format("date-time").description("Время возникновения ошибки"),
                        "path", new StringSchema().description("Путь запроса")
                    )
                )
            )
        )
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .paths(new Paths()
            .addPathItem("/**", new PathItem()
                .get(new Operation()
                    .responses(new ApiResponses()
                        .addApiResponse("400", new ApiResponse().description("Bad Request"))
                        .addApiResponse("401", new ApiResponse().description("Unauthorized"))
                        .addApiResponse("403", new ApiResponse().description("Forbidden"))
                        .addApiResponse("404", new ApiResponse().description("Not Found"))
                        .addApiResponse("500", new ApiResponse().description("Internal Server Error"))
                    )
                )
            )
        );
  }

}
