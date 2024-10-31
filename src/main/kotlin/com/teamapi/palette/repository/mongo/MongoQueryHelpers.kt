package com.teamapi.palette.repository.mongo

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.BsonField
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import org.bson.conversions.Bson
import org.springframework.data.mapping.toDotPath
import kotlin.reflect.KProperty1

infix fun <R, T : Any?> KProperty1<R, T>.eq(other: T?): Bson = Filters.eq(toDotPath(), other)
infix fun <R, T> KProperty1<R, T>.ne(other: T?): Bson = Filters.ne(toDotPath(), other)
infix fun <R, T : Any> KProperty1<R, T>.lt(other: T): Bson = Filters.lt(toDotPath(), other)
infix fun <R, T : Any> KProperty1<R, T>.lte(other: T): Bson = Filters.lte(toDotPath(), other)
infix fun <R, T : Any> KProperty1<R, T>.gt(other: T): Bson = Filters.gt(toDotPath(), other)
infix fun <R, T : Any> KProperty1<R, T>.gte(other: T): Bson = Filters.gte(toDotPath(), other)
infix fun <R, T : Any?> KProperty1<R, T>.`in`(others: List<T>): Bson = Filters.`in`(toDotPath(), others)

//fun <T : Any> MongoCollection<T>.aggregate(vararg pipeline: Bson) = aggregate(pipeline.toList())
inline fun <reified T : Any> MongoCollection<*>.aggregate(vararg pipeline: Bson) = aggregate<T>(pipeline.toList())

infix fun Bson.and(bson: Bson) = Filters.and(this, bson)

fun KProperty1<*, *>.getFirstAs(name: String): BsonField = Accumulators.first(name, literal)
fun KProperty1<*, *>.getLastAs(name: String): BsonField = Accumulators.last(name, literal)
val KProperty1<*, *>.literal: String get() = "\$${toDotPath()}"

fun KProperty1<*, *>.asc() = Sorts.ascending(name)
fun KProperty1<*, *>.desc() = Sorts.descending(name)
