package com.teamapi.palette.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseCode
import com.teamapi.palette.response.exception.CustomException
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.resource.NoResourceFoundException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.net.URLDecoder

@Component
class SessionExceptionFilter(
    private val objectMapper: ObjectMapper
) : WebFilter, Ordered {
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .onErrorResume(CustomException::class.java) {
                exchange.response.writeJson(it.responseCode, *it.formats)
            }
            .onErrorResume(NoResourceFoundException::class.java) {
                val reason = it.reason!!
                val f = reason.indexOfLast { it == ' ' }
                exchange.response.writeJson(
                    ErrorCode.ENDPOINT_NOT_FOUND,
                    URLDecoder.decode(reason.substring(f + 1, reason.length - 1), charset("utf-8"))
                )
            }
            .onErrorResume(BadCredentialsException::class.java) {
                exchange.response.writeJson(ErrorCode.INVALID_CREDENTIALS)
            }
            .onErrorResume(Exception::class.java) {
                it.printStackTrace()
                exchange.response.writeJson(ErrorCode.INTERNAL_SERVER_EXCEPTION)
            }
    }

    private fun ServerHttpResponse.writeJson(responseCode: ResponseCode, vararg format: Any?): Mono<Void> {
        statusCode = responseCode.statusCode
        headers.contentType = MediaType.APPLICATION_JSON

        return writeWith(
            Mono.just(
                bufferFactory().wrap(
                    objectMapper.writeValueAsBytes(
                        Response(
                            responseCode.statusCode.value(),
                            responseCode.message.format(*format)
                        )
                    )
                )
            )
        )
    }
}
