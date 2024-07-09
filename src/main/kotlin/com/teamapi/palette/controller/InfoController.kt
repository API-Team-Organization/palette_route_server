package com.teamapi.palette.controller

import com.teamapi.palette.dto.user.UpdateRequest
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/info")
class InfoController(
    private val userService: UserService
) {
    @GetMapping("/me")
    fun myInfo() = userService.me()
        .map { ResponseBody.ok("유저 조회 성공", it) }

    @PatchMapping("/me")
    fun updateInfo(
        @RequestBody request: UpdateRequest
    ) = userService.update(request)
        .thenReturn(Response.ok("유저 수정 성공"))
}
