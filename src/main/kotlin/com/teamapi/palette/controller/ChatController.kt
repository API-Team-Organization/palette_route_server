package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.dto.default.DefaultPageRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.ChatService
import io.swagger.v3.oas.annotations.Parameter
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
@SwaggerRequireAuthorize
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping
    fun createChat(@RequestBody request: CreateChatRequest, @RequestParam("roomId") roomId: Long) = chatService
        .createChat(roomId, request.message)
        .map { ResponseBody.ok("답변 생성 성공", it) }
//        .thenReturn(Response.ok("채팅 생성 성공"))

    @GetMapping("/{roomId}")
    @PageableAsQueryParam
    fun getChatList(@PathVariable("roomId") roomId: Long, @Parameter(hidden = true) pageRequest: DefaultPageRequest) = chatService
        .getChatList(roomId, PageRequest.of(pageRequest.page, pageRequest.size))
        .map {
            ResponseBody.ok("채팅 조회 성공", it)
        }
}
