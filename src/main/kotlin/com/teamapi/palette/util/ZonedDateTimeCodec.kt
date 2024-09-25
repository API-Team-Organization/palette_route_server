package com.teamapi.palette.util

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecConfigurationException
import java.time.Instant
import java.time.ZonedDateTime
import java.util.TimeZone

class ZonedDateTimeCodec : DateTimeBasedCodec<ZonedDateTime>() {
    override fun decode(reader: BsonReader, decoderContext: DecoderContext?): ZonedDateTime {
        return Instant.ofEpochMilli(validateAndReadDateTime(reader)).atZone(TimeZone.getDefault().toZoneId())
    }

    override fun encode(writer: BsonWriter, value: ZonedDateTime, encoderContext: EncoderContext?) {
        try {
            writer.writeDateTime(value.toInstant().toEpochMilli())
        } catch (e: ArithmeticException) {
            throw CodecConfigurationException(
                "Unsupported ZonedDateTime value '%s' could not be converted to milliseconds: %s"
                    .format(value, e.message),
                e
            )
        }
    }

    override fun getEncoderClass(): Class<ZonedDateTime> {
        return ZonedDateTime::class.java
    }
}
