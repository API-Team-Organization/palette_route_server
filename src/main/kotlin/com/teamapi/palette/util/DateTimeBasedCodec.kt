package com.teamapi.palette.util

import org.bson.BsonReader
import org.bson.BsonType
import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecConfigurationException


abstract class DateTimeBasedCodec<T> : Codec<T> {
    fun validateAndReadDateTime(reader: BsonReader): Long {
        val currentType: BsonType = reader.currentBsonType
        if (currentType != BsonType.DATE_TIME) {
            throw CodecConfigurationException(
                "Could not decode into %s, expected '%s' BsonType but got '%s'.".format(
                    encoderClass.simpleName,
                    BsonType.DATE_TIME,
                    currentType
                )
            )
        }
        return reader.readDateTime()
    }
}
