package com.teamapi.palette.controller

import com.teamapi.palette.service.ImageService
import com.teamapi.palette.util.guessExtension
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/images")
class ImageController(
    private val imageService: ImageService,
) {
    @GetMapping("/{uuid}")
    suspend fun getImageInfo(@PathVariable uuid: String): ResponseEntity<*> {
        val data = imageService.readImage(uuid)

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(guessExtension(data)))
            .contentLength(data.size.toLong())
            .body(data)
    }
}
