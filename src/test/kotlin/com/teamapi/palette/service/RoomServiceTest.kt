package com.teamapi.palette.service

import com.teamapi.palette.dto.response.room.RoomResponse
import com.teamapi.palette.entity.Room
import com.teamapi.palette.repository.chat.ChatRepository
import com.teamapi.palette.repository.qna.QnARepository
import com.teamapi.palette.repository.room.RoomRepository
import com.teamapi.palette.service.adapter.ChatEmitAdapter
import com.teamapi.palette.service.adapter.GenerativeChatAdapter
import com.teamapi.palette.service.infra.GenerativeImageService
import com.teamapi.palette.util.ExceptionReporter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class RoomServiceTest {
    private val roomRepository = mockk<RoomRepository>()
    private val chatRepository = mockk<ChatRepository>()
    private val qnaRepository = mockk<QnARepository>()
    private val chatEmitAdapter = mockk<ChatEmitAdapter>()
    private val sessionHolder = mockk<SessionHolder>()
    private val generativeChatAdapter = mockk<GenerativeChatAdapter>()
    private val generativeImageService = mockk<GenerativeImageService>()
    private val exceptionReporter = mockk<ExceptionReporter>()

    private val roomService = RoomService(
        roomRepository,
        chatRepository,
        qnaRepository,
        chatEmitAdapter,
        sessionHolder,
        generativeChatAdapter,
        generativeImageService,
        exceptionReporter
    )

    @Test
    fun createRoom() = runBlocking {
        coEvery {sessionHolder.me()} returns 1
        coEvery {roomRepository.save(any())} returns Room(1, 1, "New Chat")

        assert(roomService.createRoom().equals(RoomResponse(1, "New Chat", null)))

        coVerify { sessionHolder.me() }
        coVerify { roomRepository.save(any()) }
    }
}