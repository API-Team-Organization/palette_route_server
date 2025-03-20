package com.teamapi.palette.repository.mongo

import com.mongodb.client.model.Accumulators
import com.mongodb.client.model.BsonField
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoCollection
import kotlinx.serialization.SerialName
import org.bson.conversions.Bson
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation

private class KPropertyPath<T, U>(
    val parent: KProperty<U?>,
    val child: KProperty1<U, T>
) : KProperty<T> by child

/**
 * Recursively construct field name for a nested property.
 * @author Tjeu Kayim
 */
internal fun asString(property: KProperty<*>): String {
    return when (property) {
        is KPropertyPath<*, *> ->
            "${asString(property.parent)}.${property.child.findRealName()}"
        else -> property.findRealName()
    }
}

internal fun KProperty<*>.findRealName() = findAnnotation<SerialName>()?.value ?: name

/**
 * Builds [KPropertyPath] from Property References.
 * Refer to a nested property in an embeddable or association.
 *
 * For example, referring to the field "author.name":
 * ```
 * Book::author / Author::name isEqualTo "Herman Melville"
 * ```
 * @author Tjeu Kayim
 * @author Yoann de Martino
 * @since 2.5
 */
operator fun <T, U> KProperty<T?>.div(other: KProperty1<T, U>): KProperty<U> =
    KPropertyPath(this, other)

/**
 * Extension for [KProperty] providing an `toPath` function to render a [KProperty] in dot notation.
 *
 * @author Mark Paluch
 * @since 2.5
 * @see PropertyPath.toDotPath
 */
fun KProperty<*>.toDotPath(): String = asString(this)


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
