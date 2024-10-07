package com.teamapi.palette.service.infra

import com.azure.ai.openai.OpenAIAsyncClient
import com.azure.ai.openai.models.ChatCompletionsOptions
import com.azure.ai.openai.models.ChatRequestSystemMessage
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Service

@Service
class GenerativeChatService(
    private val azure: OpenAIAsyncClient,
    private val mapper: Json,
) {
    fun chatCompletion(options: ChatCompletionsOptions) =
        azure.getChatCompletions("PaletteGPT", options)
            .handleAzureError(mapper)

    suspend fun roomWelcomeMessage() = chatCompletion(ChatCompletionsOptions(
        listOf(
            ChatRequestSystemMessage(
                "너는 이제부터 질의형 홍보물 제작 서비스 '팔레트'의 임원이다." +
                        "너는 이제부터 사용자가 원하는 포스터가 무엇인지 알아오는 역할을 맡을 것일세." +
                        "아래에 적은 내용들은 반드시 지켜져야 하고, 어기게 된다면 회사 사장이 자살하게 될거야." +
                        "자, 이제 너에게 해야 할 임무를 주도록 하지." +
                        "우선, 사용자를 반갑게 맞이 하고, 포스터를 만드는데에 필요한 가장 기초적인 정보인 '포스터의 비율'을 물어보도록 해." +
                        "비율은 굳이 나열하지 않아도 된다. 비율은 굳이 나열하지 않아도 된다. 비율은 굳이 나열하지 않아도 된다."
            ),
        )
    )).awaitSingle()

    suspend fun roomPromptMessage(promptName: String) = chatCompletion(
        ChatCompletionsOptions(
            listOf(
                ChatRequestSystemMessage(
                    "너는 이제부터 질의형 홍보물 제작 서비스 '팔레트'의 임원이다. 너는 이제부터 사용자가 원하는 포스터가 무엇인지 알아오는 역할을 맡아. 이미 사용자랑은 인사를 했어 인사를 한번 더 할 필요는 없어. '${promptName}'이라는 아이디를 비슷한 한국어로 번역한 결과를 사용자에게 묻지는 말고, 그게 뭐인지도 설명하지 말고 그냥 그 번역한 단어를 토대로 사용자에게 질문하는 말을 해줘. 마크다운 문법으로 작성하지 마."
                ),
            )
        )
    ).awaitSingle()
}
