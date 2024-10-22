package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.request.room.UpdateRoomTitleRequest
import com.teamapi.palette.dto.response.room.QnAResponse
import com.teamapi.palette.dto.response.room.RoomResponse
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.RoomService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
@SwaggerRequireAuthorize
class RoomController(
    private val roomService: RoomService
) {
    @PostMapping
    suspend fun createRoom(): ResponseEntity<ResponseBody<RoomResponse>> {
        val data = roomService.createRoom()
        return ResponseBody.ok("룸 생성 성공", data)
    }

    @GetMapping("/list")
    suspend fun getRoomList(): ResponseEntity<ResponseBody<List<RoomResponse>>> {
        val it = roomService.getRoomList()
        return ResponseBody.ok("룸 조회 성공", it)
    }

    @PostMapping("/{roomId}/regen")
    suspend fun regenerate(@PathVariable roomId: Long) {
        roomService.regenerate(roomId)
    }

    @PatchMapping("/{roomId}/title")
    suspend fun updateRoomTitle(
        @PathVariable roomId: Long,
        @Valid @RequestBody request: UpdateRoomTitleRequest
    ): ResponseEntity<Response> {
        roomService.updateRoomTitle(roomId, request.title)
        return Response.ok("룸 업데이트 완료")
    }

    @GetMapping("/{roomId}/qna")
    suspend fun getQnAs(@PathVariable roomId: Long): ResponseEntity<ResponseBody<List<QnAResponse>>> {
        return ResponseBody.ok("룸 내 질답 리스트 조회 완료", roomService.getQnA(roomId))
    }

    @DeleteMapping("/{roomId}")
    suspend fun deleteRoom(@PathVariable roomId: Long): ResponseEntity<Response> {
        roomService.deleteRoom(roomId)
        return Response.ok("룸 삭제 완료")
    }
}
