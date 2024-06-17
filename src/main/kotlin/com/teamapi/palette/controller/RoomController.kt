package com.teamapi.palette.controller

import com.azure.core.annotation.PathParam
import com.teamapi.palette.dto.room.CreateRoomRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.RoomService
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/room")
class RoomController (
    private val roomService: RoomService
) {
    @PostMapping
    fun createRoom(@RequestBody request: CreateRoomRequest) = roomService.createRoom(request)
        .thenReturn(Response.ok("룸 생성 성공"))

    @GetMapping("/list")
    fun getRoomList() = roomService.getRoomList()
        .map { ResponseBody.ok("룸 조회 성공", it) }

    @DeleteMapping("/{roomId}")
    fun deleteRoom(@PathVariable roomId: Long) = roomService.deleteRoom(roomId)
        .thenReturn(Response.ok("룸 삭제 완료"))
}