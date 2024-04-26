package com.teamapi.palette.controller

import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.UserService
import org.springframework.web.bind.annotation.GetMapping
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
}