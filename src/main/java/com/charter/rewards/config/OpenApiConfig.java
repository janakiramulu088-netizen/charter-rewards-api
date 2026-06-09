package com.charter.rewards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the OpenAPI 3 / Swagger UI documentation.
 *
 * <p>Accessible at {@code /swagger-ui/index.html} after the application starts.
 *
 * @author Charter Assignment
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Produces the {@link OpenAPI} bean that drives the Swagger UI metadata.
     *
     * @return configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI rewardsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Rewards API")
                        .description("RESTful API for calculating customer reward points earned "
                                + "through purchase transactions. Points are awarded at 2x for "
                                + "every dollar over $100 and 1x for every dollar between $50 and $100.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Charter Assignment")
                                .email("candidate@example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
