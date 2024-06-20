package com.teamapi.palette.controller

import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.service.ChatService
import com.teamapi.palette.service.RoomService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/chat")
class ChatController (
    val chatService: ChatService
) {
    @PostMapping
    fun createChat(request: CreateChatRequest) = chatService
        .createChat(request)
        .thenReturn(Response.ok("채팅 생성 성공"))
}