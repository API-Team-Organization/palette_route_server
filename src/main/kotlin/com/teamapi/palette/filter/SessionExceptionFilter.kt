package com.teamapi.palette.filter

import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.ErrorResponse
import com.teamapi.palette.response.ResponseCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.codec.EncodingException
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
    private val objectMapper: Json
) : CoWebFilter(), Ordered {
    private val log = LoggerFactory.getLogger(SessionExceptionFilter::class.java)
    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
    override suspend fun filter(exchange: ServerWebExchange, chain: CoWebFilterChain) {
        val caught = runCatching {
            chain.filter(exchange)
        }

        if (caught.isFailure) {
            when (val e = caught.exceptionOrNull()) {
                is CustomException -> {
                    return exchange.response.writeJson(e.responseCode, *e.formats)
                }

                is NoResourceFoundException -> {
                    val reason = e.reason!!
                    val f = reason.indexOfLast { i -> i == ' ' }
                    return exchange.response.writeJson(
                        ErrorCode.ENDPOINT_NOT_FOUND,
                        URLDecoder.decode(reason.substring(f + 1, reason.length - 1), charset("utf-8"))
                    )
                }

                is BadCredentialsException -> {
                    e.printStackTrace()
                    return exchange.response.writeJson(ErrorCode.INVALID_CREDENTIALS)
                }

                is EncodingException -> {
                    log.error("Error on Serializing response", e)
                    try {
                        return exchange.response.writeJson(ErrorCode.INTERNAL_SERVER_EXCEPTION)
                    } catch (e: UnsupportedOperationException) {
                        // NO-OP

                        log.error("Response Generation failed")
                    }
                }

                is Exception -> {
                    e.printStackTrace()
                    log.error("WTFF", e)
                    return exchange.response.writeJson(ErrorCode.INTERNAL_SERVER_EXCEPTION)
                }
            }
        }
    }

    private suspend fun ServerHttpResponse.writeJson(responseCode: ResponseCode, vararg format: Any?) {
        statusCode = responseCode.statusCode
        headers.contentType = MediaType.APPLICATION_JSON

        writeWith(
            Mono.just(
                bufferFactory().wrap(
                    objectMapper.encodeToString(ErrorResponse.ofRaw(responseCode, *format)).toByteArray()
                )
            )
        ).awaitSingleOrNull()
    }
}
