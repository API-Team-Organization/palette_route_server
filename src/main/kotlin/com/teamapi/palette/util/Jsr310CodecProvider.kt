package com.teamapi.palette.util

import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

object Jsr310CodecProvider : CodecProvider {
    @Suppress("UNCHECKED_CAST")
    override fun <T> get(clazz: Class<T>, registry: CodecRegistry): Codec<T> {
        return JSR310_CODEC_MAP[clazz] as Codec<T>
    }

    private val JSR310_CODEC_MAP: MutableMap<Class<*>, Codec<*>> = HashMap()

    init {
        try {
            putCodec(ZonedDateTimeCodec())
        } catch (classNotFoundException: ClassNotFoundException) {
            // empty catch block
        }
    }

    private fun putCodec(codec: Codec<*>) {
        JSR310_CODEC_MAP[codec.encoderClass] = codec
    }
}
