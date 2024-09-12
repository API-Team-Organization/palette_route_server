package com.teamapi.palette.repository

import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.jooq.Record
import org.jooq.Record1
import org.jooq.RecordMapper
import org.jooq.ResultQuery
import reactor.core.publisher.Flux

suspend fun <E, R : Record1<E>> ResultQuery<R>.awaitAll(): List<E> =
    Flux.from(this).map { it.value1() }.collectList().awaitSingleOrNull() ?: emptyList()

suspend fun <T, R : Record> ResultQuery<R>.awaitAll(mapper: RecordMapper<in R, T>): List<T> =
    Flux.from(this).mapNotNull { mapper.map(it)!! }.collectList().awaitSingleOrNull() ?: emptyList()
