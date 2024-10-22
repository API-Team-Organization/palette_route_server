package com.teamapi.palette.service.adapter.comfy.ws

import com.teamapi.palette.util.get
import com.teamapi.palette.util.str
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement

@Serializable(with = WSMessageSerializer::class)
sealed interface ComfyWSBaseMessage {
    val type: MessageType
}

object WSMessageSerializer : JsonContentPolymorphicSerializer<ComfyWSBaseMessage>(ComfyWSBaseMessage::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ComfyWSBaseMessage> {
        val mt = try {
            val type = element["type"].str()!!
            MessageType.valueOf(type)
        } catch (e: Exception) {
            throw SerializationException(e)
        }

        return when (mt) {
            MessageType.QUEUE_STATUS -> QueueInfoMessage.serializer()
            MessageType.GENERATE_FINISH -> GenerateMessage.serializer()
        }
    }
}
