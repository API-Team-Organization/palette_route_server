package com.teamapi.palette

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*


@SpringBootApplication
class PaletteApplication

fun removeFinalModifier(field: Field) {
    val fieldAccessor = Class::class.java.declaredMethods.find { it.name == "getDeclaredFields0" } ?: throw NullPointerException()
    fieldAccessor.isAccessible = true
    val fields = fieldAccessor.invoke(Field::class.java, false) as Array<*>
    val modifier = fields.find { (it as Field).name == "modifiers" } as? Field ?: throw NullPointerException()
    modifier.isAccessible = true
    modifier.setInt(field, field.modifiers and Modifier.FINAL.inv())
}

fun main(args: Array<String>) {
    val `class` = Class.forName("org.springframework.http.codec.support.BaseDefaultCodecs")
    val jackson2Enabled = `class`.getDeclaredField("jackson2Present")
    jackson2Enabled.isAccessible = true
    removeFinalModifier(jackson2Enabled)
    jackson2Enabled.setBoolean(`class`, false)

    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))

    System.setProperty("org.jooq.no-logo", "true")
    System.setProperty("org.jooq.no-tips", "true")
    runApplication<PaletteApplication>(*args)
}
