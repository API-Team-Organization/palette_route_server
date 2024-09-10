package com.teamapi.palette.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
@SecurityScheme(
    name = "X-AUTH-Token",
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.HEADER,
)
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
    }
}
