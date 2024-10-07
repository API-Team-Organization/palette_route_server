package com.teamapi.palette.entity.qna

import com.teamapi.palette.entity.consts.PromptType
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import kotlinx.serialization.Contextual
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bson.types.ObjectId

@Serializable
sealed interface PromptData {
    val type: PromptType
    val question: ChatQuestion
    val answer: ChatAnswer?
    val promptName: String
    val id: ObjectId

    fun <T : ChatAnswer> fulfillAnswer(answer: T): PromptData

    @Serializable
    data class Selectable(
        override val promptName: String,
        override val question: ChatQuestion.SelectableQuestion,
        override val answer: ChatAnswer.SelectableAnswer? = null
    ) : PromptData {
        override val type: PromptType = PromptType.SELECTABLE
        @Contextual @SerialName("_id") override val id: ObjectId = ObjectId.get()

        override fun <T : ChatAnswer> fulfillAnswer(answer: T): PromptData {
            return copy(answer = answer as? ChatAnswer.SelectableAnswer)
        }
        @Serializable
        data class Choice(val id: String, val displayName: String)
    }

    @Serializable
    data class Grid(
        override val promptName: String,
        override val question: ChatQuestion.GridQuestion,
        override val answer: ChatAnswer.GridAnswer? = null
    ) : PromptData {
        override val type: PromptType = PromptType.GRID
        @Contextual @SerialName("_id") override val id: ObjectId = ObjectId.get()

        override fun <T : ChatAnswer> fulfillAnswer(answer: T): PromptData {
            return copy(answer = answer as? ChatAnswer.GridAnswer)
        }
    }

    @Serializable
    data class UserInput(
        override val promptName: String,
        override val answer: ChatAnswer.UserInputAnswer? = null
    ) : PromptData {
        override val question = ChatQuestion.UserInputQuestion
        override val type: PromptType = PromptType.USER_INPUT
        @Contextual @SerialName("_id") override val id: ObjectId = ObjectId.get()

        override fun <T : ChatAnswer> fulfillAnswer(answer: T): PromptData {
            return copy(answer = answer as? ChatAnswer.UserInputAnswer)
        }
    }
}

@Serializable
sealed class ChatQuestion(val type: PromptType) {
    @Serializable
    data class SelectableQuestion(val choices: List<PromptData.Selectable.Choice>) : ChatQuestion(PromptType.SELECTABLE)

    @Serializable
    data class GridQuestion(val xSize: Int, val ySize: Int) : ChatQuestion(PromptType.GRID)

    @Serializable
    data object UserInputQuestion : ChatQuestion(PromptType.USER_INPUT)
}

@Serializable
sealed class ChatAnswer(val type: PromptType) {
    @Serializable
    data class SelectableAnswer(val choiceId: String) : ChatAnswer(PromptType.SELECTABLE)

    @Serializable
    data class GridAnswer(val choice: List<Int>) : ChatAnswer(PromptType.GRID)

    @Serializable
    data class UserInputAnswer(val input: String) : ChatAnswer(PromptType.USER_INPUT)

    object ChatAnswerSerializer : JsonContentPolymorphicSerializer<ChatAnswer>(ChatAnswer::class) {
        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ChatAnswer> {
            val m = element.jsonObject["type"]?.jsonPrimitive?.content?.let {
                try {
                    PromptType.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    throw CustomException(
                        ErrorCode.MESSAGE_TYPE_NOT_MATCH,
                        it,
                        PromptType.entries.joinToString("', '")
                    )
                }
            }
                ?: throw IllegalArgumentException("$element")
            return when (m) {
                PromptType.USER_INPUT -> UserInputAnswer.serializer()
                PromptType.SELECTABLE -> SelectableAnswer.serializer()
                PromptType.GRID -> GridAnswer.serializer()
            }
        }
    }
}
