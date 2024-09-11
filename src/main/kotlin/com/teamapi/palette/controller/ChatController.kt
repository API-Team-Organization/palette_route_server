package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.dto.chat.ChatUpdateResponse
import com.teamapi.palette.dto.chat.CreateChatRequest
import com.teamapi.palette.dto.default.DefaultPageRequest
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.ChatService
import io.swagger.v3.oas.annotations.Parameter
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chat")
@SwaggerRequireAuthorize
class ChatController(
    private val chatService: ChatService,
) {
    @PostMapping
    suspend fun createChat(
        @RequestBody request: CreateChatRequest,
        @RequestParam("roomId") roomId: Long
    ): ResponseEntity<ResponseBody<ChatUpdateResponse>> {
        val data = chatService.createChat(roomId, request.message)
        return ResponseBody.ok("답변 생성 성공", data)
    }

    @PageableAsQueryParam
    suspend fun getChatList(
        @PathVariable("roomId") roomId: Long,
        @Parameter(hidden = true) pageRequest: DefaultPageRequest
    ): ResponseEntity<ResponseBody<List<ChatResponse>>> {
        val data = chatService.getChatList(
            roomId = roomId,
            pageable = PageRequest.of(pageRequest.page, pageRequest.size)
        )
        return ResponseBody.ok("채팅 조회 성공", data)
    }

    @GetMapping("/my-image")
    fun getMyImage(@RequestParam pageNumber: Int, @RequestParam pageSize: Int) = chatService
        .getMyImage(pageNumber, pageSize)
}
