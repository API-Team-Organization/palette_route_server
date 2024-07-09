package com.teamapi.palette.controller

import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.ChatService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping
    fun createChat(@RequestBody request: CreateChatRequest) = chatService
        .createChat(request)
        .map { ResponseBody.ok("답변 생성 성공", it) }
//        .thenReturn(Response.ok("채팅 생성 성공"))

    @GetMapping("/{roomId}")
    fun getChatList(@PathVariable("roomId") roomId: Long) = chatService
        .getChatList(roomId)
        .map {
            ResponseBody.ok("채팅 조회 성공", it)
        }
}
