package com.teamapi.palette.controller

import com.teamapi.palette.dto.room.UpdateRoomTitleRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.RoomService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
class RoomController (
    private val roomService: RoomService
) {
    @PostMapping
    fun createRoom() = roomService.createRoom()
        .thenReturn(Response.ok("룸 생성 성공"))

    @GetMapping("/list")
    fun getRoomList() = roomService.getRoomList()
        .map { ResponseBody.ok("룸 조회 성공", it) }

    @PatchMapping("/title")
    fun updateRoomTitle(
        @RequestBody updateRoomTitleRequest: UpdateRoomTitleRequest
    ) = roomService
        .updateRoomTitle(updateRoomTitleRequest)
        .thenReturn(Response.ok("룸 업데이트 완료"))

    @DeleteMapping("/{roomId}")
    fun deleteRoom(@PathVariable roomId: Long) = roomService.deleteRoom(roomId)
        .thenReturn(Response.ok("룸 삭제 완료"))
}