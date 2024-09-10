package com.teamapi.palette.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.Ordered
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.resource.NoResourceFoundException
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.net.URLDecoder

@Component
class SessionExceptionFilter(
    private val objectMapper: ObjectMapper
) : CoWebFilter(), Ordered {
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        val caught = runCatching {
            chain.filter(exchange)
        }

        if (caught.isFailure) {
            val e = caught.exceptionOrNull()
            if (e is CustomException) {
                return exchange.response.writeJson(e.responseCode, *e.formats)
            } else if (e is NoResourceFoundException) {
                val reason = e.reason!!
                val f = reason.indexOfLast { i -> i == ' ' }
                return exchange.response.writeJson(
                    ErrorCode.ENDPOINT_NOT_FOUND,
                    URLDecoder.decode(reason.substring(f + 1, reason.length - 1), charset("utf-8"))
                )
            } else if (e is BadCredentialsException) {
                e.printStackTrace()
                return exchange.response.writeJson(ErrorCode.INVALID_CREDENTIALS)
            } else if (e is Exception) {
                e.printStackTrace()
                return exchange.response.writeJson(ErrorCode.INTERNAL_SERVER_EXCEPTION)
            }
        }
    }

    private suspend fun ServerHttpResponse.writeJson(responseCode: ResponseCode, vararg format: Any?) {
        statusCode = responseCode.statusCode
        headers.contentType = MediaType.APPLICATION_JSON

        writeWith(
            Mono.just(
                bufferFactory().wrap(
                    objectMapper.writeValueAsBytes(
                        Response(
                            responseCode.statusCode.value(), responseCode.message.format(*format)
                        )
                    )
                )
            )
        ).awaitSingle()
    }
}
