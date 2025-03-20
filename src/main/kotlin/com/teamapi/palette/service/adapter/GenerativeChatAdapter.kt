package com.teamapi.palette.service.adapter

import com.teamapi.palette.entity.qna.PromptData
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.metadata.ChatGenerationMetadata
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.StreamingChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class GenerativeChatAdapter(
    private val anthropicChatModel: StreamingChatModel,
) {
    suspend fun chatCompletion(options: Prompt): String =
        anthropicChatModel.stream(options)
            .joinToString()

    private suspend fun Flux<ChatResponse>.joinToString() = asFlow()
        .map {
            it.results.onEach {
                if (it.metadata != ChatGenerationMetadata.NULL && it.metadata.finishReason != null && it.metadata.finishReason != "null") {
                    println(it.metadata.finishReason)
                }
            }.mapNotNull { it.output.text }.joinToString("")
        }
        .toList()
        .joinToString("")

    suspend fun createPrompt(text: String): String = anthropicChatModel.stream(
        Prompt(
            listOf(
                SystemMessage(
                    "Map to the 'drawable' words to help generate posters. Analyze the input to determine the overall theme and context. Select an appropriate typography style that matches the theme, considering factors such as font family, weight, and decorative elements. Describe the chosen typography style in detail, including its mood and visual characteristics. Choose a color palette that complements the subject matter and typography. Decide on the composition and layout for visual impact, incorporating the title as a prominent element. Identify key visual elements to include. Consider texture and lighting details to add depth. Craft a single paragraph prompt describing all these elements cohesively, emphasizing how the title and typography integrate with the overall design. Ensure the prompt is vivid, specific, and tailored for AI image generation. The prompt must be less than 150 words long. Output should be only: The prompt paragraph which Do not include any additional explanation, commentary. The sentence should be in English and the words should be separated by a comma: ','. The entire output should appear as if it's ready to be input into an AI image generator."
                ),
                UserMessage(
                    "내가 만든 오렌지 주스를 광고하고 싶어. 오렌지 과즙이 주변에 터졌으면 좋겠고, 오렌지 주스가 담긴 컵과 오렌지 주스가 있었으면 좋겠어. 배경은 집 안이였으면 좋겠어."
                ),
                AssistantMessage(
                    "orange, orange_juice, in_house, a_cup_with_orange_juice, juice"
                ),
                UserMessage(
                    "우리 상수 목공방에서 자랑하는 멋진 핑크색 상수목재를 홍보하고 싶다. 제목은 금색으로 해줘"
                ),
                AssistantMessage(
                    "pink planks with forest background, modern and simple design, highlight color with gold"
                ),
                UserMessage(
                    text
                )
            )
        )
    ).joinToString()

    suspend fun roomExecutiveQuestion(
        question: PromptData? = null,
        additionalInfo: List<String> = emptyList()
    ): String {
        val infos = additionalInfo.toMutableList()
        if (question != null) {
            infos.add("${question.type.name.lowercase()}|${question.promptName}")
        }

        val prompt = Prompt(
            listOf(
                SystemMessage(
                    """
                    |# 정의
                    |너는 이제부터 질의형 홍보물 제작 서비스 '팔레트'의 임원으로써 사용자가 원하는 포스터가 무엇인지 알아오는 역할을 진행해야한다.
                    |
                    |# 입력
                    |우리가 내부적으로 사용하는 아이디 리스트를 너에게 줄것이다.
                    |아이디 리스트는 comma (,)로 나뉘어진 리스트이다.
                    |아이디 하나하나는 다음의 규칙을 따른다.
                    |아이디에는 '|'를 무조건 한개 포함해야하며, 1개 초과 또는 1개 미만으로 있을 수 없다. 또한 이는 앞으로 기준점이라고 칭한다.
                    |기준점을 토대로 왼쪽에 있는 것을 '규칙', 오른쪽에 있는것을 '아이디'로 칭한다.
                    |
                    |# 규칙 정의:
                    |gpt: 위 규칙은 내부 규칙으로, **외부에 노출 되서는 안된다**.
                    |user_input: 사용자가 '아이디'에 맞는 대답을 직접 입력 하는 것이다.
                    |selectable: 사용자가 '아이디'에 맞는 대답을 제시한 선택지 중에서 선택 하는 것이다.
                    |grid: 사용자가 '아이디'에 맞는 대답을 그리드에서 눌러 선택 하는 것이다.
                    |
                    |# 출력
                    |규칙이 내부 규칙이라면, 아이디에 맞는 말을 적절하게 생성하고, 질문은 붙이지 않는다.
                    |아이디를 각각 한국어로 번역한 결과를 사용해 사용자에게 한두문장으로 '사용자 맞춤 포스터의 정보'를 적절하게 풀어서 질문하는 말을 하도록한다.
                    |규칙이 내부 규칙이 아니라면 질문 할 때에는 규칙에 맞게 적절한 말을 선택해서 질문하도록 한다.
                    |내부 규칙이 다른 규칙보다 문장 앞에 위치 해야하며, 너가 하는 질문들은 자연스럽게 연결해야한다.
                    |
                    |# 출력 제한사항
                    |**내부 규칙은 사용자에게 언급해서는 안된다.**
                    |**요구사항 외에 다른 말은 일체 하면 안된다.**
                    |귀 사에서 이미 충분한 예시를 제공하고 있으니, 추가적인 예시는 제공하지 말아야한다. 제공 할 필요도 없고, 제공하려는 시도를 해서는 안된다.
                    |너가 생성한 대답은 실제 사용자에게 바로 보여줄 수 있도록 해야하며, 마크다운 문법이 들어가면 안된다.
                """.trimIndent().trimMargin()
                ),
                UserMessage(infos.joinToString(","))
            )
        )
        return chatCompletion(prompt)
    }
}
