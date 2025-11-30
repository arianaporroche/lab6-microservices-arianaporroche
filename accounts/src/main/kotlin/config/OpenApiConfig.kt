package accounts.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Accounts Service API")
                    .version("1.0")
                    .description("API REST del microservicio Accounts")
                    .contact(
                        Contact()
                            .name("Tu Nombre")
                            .email("tuemail@example.com")
                    )
            )
    }
}
