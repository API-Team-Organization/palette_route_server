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
import com.teamapi.palette.repository.UserRepository
import com.teamapi.palette.response.ErrorCode
import com.teamapi.palette.response.exception.CustomException
import com.teamapi.palette.util.findUser
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val sessionHolder: SessionHolder,
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val azure: OpenAIAsyncClient,
    private val mapper: ObjectMapper
) {
    fun createChat(request: CreateChatRequest): Mono<ChatUpdateResponse> {
        return sessionHolder.me().findUser(userRepository).flatMap {
            chatRepository.save(
                Chat(
                    message = request.message,
                    datetime = LocalDateTime.now(),
                    roomId = request.roomId,
                    userId = it.id!!,
                    isAi = false
                )
            )
        }.then(Flux.zip(
            createUserReturn(request.message), draw(request.message), sessionHolder.me()
        ).flatMap {
            chatRepository.saveAll(
                listOf(
                    Chat(
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
        }.map { res ->
            ChatResponse(
                res.id!!, res.message, res.datetime, res.roomId, res.userId, res.isAi, res.resource
            )
        }.collectList().map { ChatUpdateResponse(it) })
    }

    fun getChatList(roomId: Long): Mono<List<ChatResponse>> {
        return sessionHolder.me().findUser(userRepository).flatMap { user ->
            roomRepository.findById(roomId).switchIfEmpty {
                error(CustomException(ErrorCode.ROOM_NOT_FOUND))
            }.flatMapMany { room ->
                if (room.userId != user.id) {
                    return@flatMapMany Flux.error(CustomException(ErrorCode.FORBIDDEN))
                }

                chatRepository.findByRoomId(roomId)
            }.map {
                ChatResponse(
                    it.id!!, it.message, it.datetime, it.roomId, it.userId, it.isAi, it.resource
                )
            }.collectList()
        }
    }

    // TODO: Apply Comfy, Prompt enhancing - in next semester
    fun draw(query: String) = azure.getImageGenerations("Dalle3", ImageGenerationOptions(query))
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
                    "사용자가 입력한 키워드를 사용해 적절하게 이미지를 생성한다는 말을 만들어줘. 출력은 한국어로 해줘"
                ), ChatRequestUserMessage(
                    "내가 만든 오렌지 주스를 광고하고 싶어. 오렌지 과즙이 주변에 터졌으면 좋겠고, 오렌지 주스가 담긴 컵과 오렌지 주스가 있었으면 좋겠어. 배경은 집 안이였으면 좋겠어."
                ), ChatRequestAssistantMessage(
                    "알겠습니댜. 상큼한 오렌지 주스를 홍보하는 홍보물을 제작 해 드리겠습니다."
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
    ).onErrorResume(HttpResponseException::class.java) {
        Mono.just(mapper.convertValue<AzureExceptionResponse>(it.value))
            .flatMap { Mono.error<ChatCompletions>(CustomException(ErrorCode.CHAT_FILTERED)) }
            .onErrorResume(Throwable::class.java) {
                Mono.error(CustomException(ErrorCode.INTERNAL_SERVER_EXCEPTION))
            }
    }
}
