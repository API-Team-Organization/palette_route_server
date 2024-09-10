package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.room.RoomResponse
import com.teamapi.palette.dto.room.UpdateRoomTitleRequest
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

    @PatchMapping("/title")
    suspend fun updateRoomTitle(
        @Valid @RequestBody updateRoomTitleRequest: UpdateRoomTitleRequest
    ): ResponseEntity<Response> {
        roomService.updateRoomTitle(updateRoomTitleRequest)
        return Response.ok("룸 업데이트 완료")
    }

    @DeleteMapping("/{roomId}")
    suspend fun deleteRoom(@PathVariable roomId: Long): ResponseEntity<Response> {
        roomService.deleteRoom(roomId)
        return Response.ok("룸 삭제 완료")
    }
}
