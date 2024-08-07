package com.teamapi.palette.service

import com.azure.ai.openai.OpenAIAsyncClient
import com.azure.ai.openai.models.*
import com.azure.core.exception.HttpResponseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.teamapi.palette.dto.chat.AzureExceptionResponse
import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.dto.chat.ChatUpdateResponse
import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.entity.Chat
import com.teamapi.palette.repository.ChatRepository
import com.teamapi.palette.repository.RoomRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.util.validateUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val sessionHolder: SessionHolder,
    private val roomRepository: RoomRepository,
    private val azure: OpenAIAsyncClient,
    private val mapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(ChatService::class.java)
    fun createChat(request: CreateChatRequest): Mono<ChatUpdateResponse> {
        return roomRepository.findById(request.roomId)
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.ROOM_NOT_FOUND)))
            .validateUser(sessionHolder)

            .then(createUserReturn(request.message)).flatMapMany {
                Flux.zip(Mono.just(it), draw(request.message), sessionHolder.me())
            }.flatMap {
                chatRepository.saveAll(
                    listOf(
                        Chat(
                            message = request.message,
                            datetime = LocalDateTime.now(),
                            roomId = request.roomId,
                            userId = it.t3,
                            isAi = false
                        ), Chat(
                            message = it.t2.data[0].url,
                            datetime = LocalDateTime.now(),
                            resource = "IMAGE",
                            roomId = request.roomId,
                            userId = it.t3,
                            isAi = true
                        ), Chat(
                            message = it.t1.choices[0].message.content,
                            datetime = LocalDateTime.now(),
                            roomId = request.roomId,
                            userId = it.t3,
                            isAi = true
                        )
                    )
                )
            }.filter { it.isAi }.map { res ->
                ChatResponse(
                    res.id!!, res.message, res.datetime, res.roomId, res.userId, res.isAi, res.resource
                )
            }.collectList().map { ChatUpdateResponse(it) }
    }

    fun getChatList(roomId: Long): Mono<List<ChatResponse>> {
        return roomRepository.findById(roomId)
            .switchIfEmpty(Mono.error(CustomException(ErrorCode.ROOM_NOT_FOUND)))
            .validateUser(sessionHolder)

            .flatMapMany { chatRepository.findByRoomId(it.id!!) }
            .map { ChatResponse(it.id!!, it.message, it.datetime, it.roomId, it.userId, it.isAi, it.resource) }
            .collectList()
    }

    // TODO: Apply Comfy, Prompt enhancing - in next semester
    fun draw(query: String) = azure.getImageGenerations("Dalle3", ImageGenerationOptions(query)).handleAzureError()
//        webui.txt2Img(
//        Txt2ImageOptions(
//            samplerName = SamplingMethod.EULER_A,
//            scheduler = Scheduler.SGM_UNIFORM,
//            steps = 10,
//            width = 768,
//            height = 1024,
//            cfgScale = 6.5,
//            prompt = "score_9, score_8, score_7_up, simplified, (($query):0.9), <lora:pytorch_lora_weights:1>, <lora:LineArt Mono Style LoRA_Pony XL v6:1>",
//            negativePrompt = "bad_hands, aidxlv05_neg, (nsfw:1.5), nude, furry, text",
//        )
//    )

    fun createUserReturn(text: String): Mono<ChatCompletions> = chatCompletion(
        ChatCompletionsOptions(
            listOf(
                ChatRequestSystemMessage(
                    "너는 세계에서 제일 가는 유명한 화가야. 이제부터 너는 그림을 그릴 수 있고, 그림을 못그린다는 절대 소리는 할 수 없어. 적당히 말ㅇㄹ 꾸며내어서 사용자가 입력한 키워드를 요약해 적절하게 이미지를 생성한다는 말을 만들어줘. 최대한 간결하게 1~2문장만 출력해줘. 출력은 한국어로 해주고, 반말하지말고 공손하게 존댓말로 해"
                ), ChatRequestUserMessage(
                    text
                )
            )
        )
    )

//    fun createPrompt(text: String) = chatCompletion(
//        ChatCompletionsOptions(
//            listOf(
//                ChatRequestSystemMessage(
//                    "You must enter a sentence in Korean or English and extract the keywords for the sentence. All words should be in English and the words should be separated by a semicolon (',') and an underscore ('_') if there is a space in a word. Also, there should be no words other than a semicolon and any words. Produce a few more related words if the number of extracted words is less than 5. Map to  the 'drawable' words to help generate posters. Give all words lowercase."
//                ),
//                ChatRequestUserMessage(
//                    "내가 만든 오렌지 주스를 광고하고 싶어. 오렌지 과즙이 주변에 터졌으면 좋겠고, 오렌지 주스가 담긴 컵과 오렌지 주스가 있었으면 좋겠어. 배경은 집 안이였으면 좋겠어."
//                ),
//                ChatRequestAssistantMessage(
//                    "orange, orange_juice, in_house, a_cup_with_orange_juice, juice"
//                ),
//                ChatRequestUserMessage(
//                    text
//                )
//            )
//        )
//    )

    private fun chatCompletion(options: ChatCompletionsOptions) = azure.getChatCompletions(
        "PaletteGPT", options
    ).handleAzureError()

    private fun <T> Mono<T>.handleAzureError() = onErrorMap(HttpResponseException::class.java) {
        try {
            if (mapper.convertValue<AzureExceptionResponse>(it.value).error.innerError.code != "ResponsibleAIPolicyViolation") throw it
            CustomException(ErrorCode.CHAT_FILTERED)
        } catch (e: Throwable) {
            log.error("Error on Azure: ", it)
            log.error("Body: {}", it.value)
            CustomException(ErrorCode.INTERNAL_SERVER_EXCEPTION)
        }
    }
}
