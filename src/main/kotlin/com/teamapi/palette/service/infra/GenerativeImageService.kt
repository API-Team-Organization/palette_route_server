package com.teamapi.palette.service.infra

import com.teamapi.palette.entity.Room
import com.teamapi.palette.entity.chat.Chat
import com.teamapi.palette.entity.consts.ChatState
import com.teamapi.palette.entity.qna.ChatAnswer
import com.teamapi.palette.entity.qna.PromptData
import com.teamapi.palette.entity.qna.QnA
import com.teamapi.palette.service.adapter.BlobSaveAdapter
import com.teamapi.palette.service.adapter.ChatEmitAdapter
import com.teamapi.palette.service.adapter.GenerativeChatAdapter
import com.teamapi.palette.service.adapter.GenerativeImageAdapter
import com.teamapi.palette.service.adapter.comfy.GenerateRequest
import com.teamapi.palette.service.adapter.comfy.ws.GenerateMessage
import com.teamapi.palette.service.adapter.comfy.ws.QueueInfoMessage
import com.teamapi.palette.util.ExceptionReporter
import com.teamapi.palette.ws.actor.SinkActor
import org.springframework.stereotype.Service
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Service
class GenerativeImageService(
    private val actor: SinkActor,
    private val chatEmitAdapter: ChatEmitAdapter,
    private val generativeChatAdapter: GenerativeChatAdapter,
    private val generativeImageAdapter: GenerativeImageAdapter,
    private val blobSaveAdapter: BlobSaveAdapter,
    private val exceptionReporter: ExceptionReporter,
) {
    private val includesKorean = Regex("[ㄱ-ㅎ가-힣]")
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun generateImage(qna: QnA, room: Room, me: Long) {
        val release = qna.qna

        val title = release.find { it.promptName == "title" }!!.answer as ChatAnswer.UserInputAnswer
        val explain = release.find { it.promptName == "product_explanation" }!!.answer as ChatAnswer.UserInputAnswer
        val aspectQnA = release.find { it.promptName == "aspect_ratio" }!! as PromptData.Selectable
        val aspectAns = aspectQnA.answer!!
        val hvQnA = release.find { it.promptName == "horizontal_or_vertical" }!! as PromptData.Selectable
        val hvAns = hvQnA.answer!!
        val grid = release.find { it.promptName == "title_position" }!!.answer as ChatAnswer.GridAnswer

        val (width, height) = when (aspectAns.choiceId) {
            "DISPLAY" -> 1820 to 1024
            "PAPER" -> 1444 to 1024
            "SQUARE" -> 1024 to 1024
            else -> 1365 to 1024 // TABLET
        }
        var guaranteed: String? = null
        try {
            var prompt = generativeChatAdapter.createPrompt(explain.input)
            println(prompt)

            val hasKorean = includesKorean.find(title.input) != null
            if (hasKorean) {
                prompt += "don't write any word or text"
            } else {
                val posStr = when (grid.choice[0]) {
                    0 -> "top"
                    1 -> "middle"
                    else /*2*/ -> "bottom"
                }
                prompt += "Please write down the title '${title.input}', place the title typography at $posStr"
            }

            val generated = generativeImageAdapter.draw(
                GenerateRequest(
                    title.input,
                    grid.choice[0],
                    if (hvAns.choiceId == "HORIZONTAL") width else height,
                    if (hvAns.choiceId == "HORIZONTAL") height else width,
                    prompt,
                    hasKorean
                )
            )

            generated.collect {
                when (it) {
                    is QueueInfoMessage -> {
                        actor.addQueue(room.id!!, it.position, true)
                    }
                    is GenerateMessage -> {
                        if (it.result) {
                            guaranteed = it.image!!
                        }
                    }
                }
            }
        } catch (e: Exception) {
            exceptionReporter.reportException("Error Generating Image", e)
            e.printStackTrace()
            guaranteed = null
        }
        if (guaranteed == null) {
            chatEmitAdapter.emitChat(
                Chat(
                    resource = ChatState.CHAT,
                    roomId = room.id!!,
                    userId = me,
                    isAi = true,
                    message = "이미지를 생성하는 도중 오류가 발생하였어요. ;.;"
                )
            )
            return
        }

        try {
            val uploaded = blobSaveAdapter.save(Base64.decode(guaranteed!!))

            chatEmitAdapter.emitChat(
                Chat(
                    resource = ChatState.IMAGE,
                    roomId = room.id!!,
                    userId = me,
                    isAi = true,
                    message = uploaded.blobUrl
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
