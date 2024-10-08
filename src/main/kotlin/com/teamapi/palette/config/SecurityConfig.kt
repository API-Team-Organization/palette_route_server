package com.teamapi.palette.config

import com.teamapi.palette.entity.consts.UserState
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.BaseResponse
import com.teamapi.palette.response.ErrorResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
class SecurityConfig(private val json: Json) {
    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    private inline fun <reified T : BaseResponse> responseJson(exchange: ServerWebExchange, data: T): Mono<Void> {
        val res = exchange.response
        res.statusCode = HttpStatus.valueOf(data.code)
        res.headers.contentType = MediaType.APPLICATION_JSON

        return res.writeWith(Flux.just(res.bufferFactory().wrap(json.encodeToString(data).toByteArray())))
    }


    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 3)
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange {
                it
                    .pathMatchers("/auth/login", "/auth/register").permitAll()
                    .pathMatchers("/v3/api-docs/**", "/external/**", "swagger").permitAll()
                    .pathMatchers("/auth/resign", "/auth/session", "/auth/logout").authenticated()
                    .pathMatchers("/auth/verify", "/auth/resend").hasRole(UserState.CREATED.name)
                    .anyExchange().hasRole(UserState.ACTIVE.name)
            }
            .exceptionHandling {
                it.authenticationEntryPoint { exchange, _ ->
                    responseJson(
                        exchange,
                        ErrorResponse.ofRaw(ErrorCode.INVALID_SESSION)
                    )
                }
                    .accessDeniedHandler { exchange, _ ->
                        responseJson(
                            exchange,
                            ErrorResponse.ofRaw(ErrorCode.FORBIDDEN, exchange.request.path.value())
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
