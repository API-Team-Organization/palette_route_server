package com.teamapi.palette.controller

import com.teamapi.palette.response.Response
import com.teamapi.palette.service.RoomService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/room")
class RoomController (
    private val roomService: RoomService
) {
    @PostMapping
    fun createRoom() = roomService.createRoom()
        .thenReturn(Response.ok("룸 생성 성공"))
}