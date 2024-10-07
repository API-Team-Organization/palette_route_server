package com.teamapi.palette.repository.qna

import com.teamapi.palette.entity.qna.QnA

interface QnARepository {
    suspend fun getQnAByRoomId(roomId: Long): QnA?
    suspend fun create(prompt: QnA): QnA
}
