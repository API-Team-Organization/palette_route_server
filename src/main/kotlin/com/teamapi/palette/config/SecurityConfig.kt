package com.teamapi.palette.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.Response
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class SecurityConfig(private val objectMapper: ObjectMapper) {
    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    fun <T : Response> json(exchange: ServerWebExchange, data: T): Mono<Void> {
        val res = exchange.response
        res.statusCode = HttpStatus.valueOf(data.code)
        res.headers.contentType = MediaType.APPLICATION_JSON

        return res.writeWith(Flux.just(res.bufferFactory().wrap(objectMapper.writeValueAsBytes(data))))
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 3)
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange {
                it
                    .pathMatchers("/auth/login", "/auth/register").permitAll()
                    .pathMatchers("/v3/api-docs/**", "/webjars/swagger-ui/**").permitAll()
                    .pathMatchers("/auth/resign", "/auth/session", "/auth/logout").permitAll()
                    .pathMatchers("/auth/verify", "/auth/resend").hasRole(UserState.CREATED.name)
                    .anyExchange().hasRole(UserState.ACTIVE.name)
            }
            .exceptionHandling {
                it.authenticationEntryPoint { exchange, ex ->
                    json(
                        exchange,
                        Response(
                            ErrorCode.INVALID_SESSION.statusCode.value(),
                            ErrorCode.INVALID_SESSION.message
                        )
                    )
                }
                    .accessDeniedHandler { exchange, denied ->
                        denied.printStackTrace()
                        json(
                            exchange,
                            Response(
                                ErrorCode.ENDPOINT_NOT_FOUND.statusCode.value(),
                                ErrorCode.ENDPOINT_NOT_FOUND.message
                            )
                        )
                    }
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration()
        corsConfiguration.allowedOrigins = listOf("*")
        corsConfiguration.allowedHeaders = listOf("*")
        corsConfiguration.allowedMethods = listOf("*")
        corsConfiguration.exposedHeaders = listOf("*")
        val urlBasedCorsConfigurationSource = UrlBasedCorsConfigurationSource()
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)
        return urlBasedCorsConfigurationSource
    }

    @Bean
    fun authManager(userDetailsService: ReactiveUserDetailsService): ReactiveAuthenticationManager {
        return UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)
            .apply {
                setPasswordEncoder(bCryptPasswordEncoder())
            }
    }
}
