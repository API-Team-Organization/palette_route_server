package com.teamapi.palette

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*

@SpringBootApplication
class PaletteApplication {
    @PostConstruct
    fun init() {
        // Timezone Fix
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }
}

fun main(args: Array<String>) {
    runApplication<PaletteApplication>(*args)
}
