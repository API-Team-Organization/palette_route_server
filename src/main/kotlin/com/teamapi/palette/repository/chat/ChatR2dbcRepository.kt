package com.teamapi.palette.repository.chat

import com.teamapi.palette.entity.chat.Chat
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ChatR2dbcRepository : CoroutineCrudRepository<Chat, Long>
