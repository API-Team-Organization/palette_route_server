package com.teamapi.palette.repository

import com.teamapi.palette.entity.Chat
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface ChatRepository : R2dbcRepository<Chat, Long> {
}