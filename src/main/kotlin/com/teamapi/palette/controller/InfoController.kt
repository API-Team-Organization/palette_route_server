package com.teamapi.palette.controller

import com.teamapi.palette.annotations.SwaggerRequireAuthorize
import com.teamapi.palette.dto.request.user.UserUpdateRequest
import com.teamapi.palette.dto.response.user.UserResponse
import com.teamapi.palette.response.Response
import com.teamapi.palette.response.ResponseBody
import com.teamapi.palette.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/info")
@SwaggerRequireAuthorize
class InfoController(
    private val userService: UserService
) {
    @GetMapping("/me")
    suspend fun myInfo(): ResponseEntity<ResponseBody<UserResponse>> {
        val data = userService.me()
        return ResponseBody.ok("유저 조회 성공", data)
    }

    @PatchMapping("/me")
    suspend fun updateInfo(
        @RequestBody request: UserUpdateRequest
    ): ResponseEntity<Response> {
        userService.update(request)
        return Response.ok("유저 수정 성공")
    }
}
