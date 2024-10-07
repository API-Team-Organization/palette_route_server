package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.request.chat.CreateChatRequest
import com.teamapi.palette.dto.request.default.DefaultPageRequest
import com.teamapi.palette.dto.response.ChatResponses.*
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.response.ResponseList
import com.teamapi.palette.service.ChatService
import io.swagger.v3.oas.annotations.Parameter
import kotlinx.datetime.Clock
import org.springdoc.core.converters.models.PageableAsQueryParam
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
    ): ResponseEntity<Response> {
        chatService.createChat(roomId, request.message)
        return Response.ok("답변 요청 성공")
    }

    @GetMapping("/{roomId}")
    suspend fun getChatList(
        @PathVariable("roomId") roomId: Long,
        @RequestParam(required = false) before: String = Clock.System.now().toString(),
        @RequestParam(required = false) size: Long = 25
    ): ResponseEntity<ResponseList> {
        val data = chatService.getChatList(
            roomId = roomId,
            lastId = before,
            size = size
        )
        return ResponseList.ok("채팅 조회 성공", data)
    }


    @PageableAsQueryParam
    @GetMapping("/my-image")
    suspend fun getMyImage(
        @Parameter(hidden = true) pageRequest: DefaultPageRequest
    ): ResponseEntity<ResponseBody> {
        val data = chatService.getMyImage(pageRequest.page, pageRequest.size)
        return ResponseBody.ok("이미지 리스트 조회 완료", ImageListResponse(data))
    }
}
