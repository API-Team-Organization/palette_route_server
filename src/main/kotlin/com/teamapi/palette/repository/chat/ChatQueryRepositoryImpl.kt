package com.teamapi.palette.repository.chat

import com.teamapi.palette.dto.chat.ChatResponse
import com.teamapi.palette.repository.awaitAll
import org.jooq.DSLContext
import java.time.LocalDateTime
import com.teamapi.palette.entity.tables.TblChat.TBL_CHAT as CHAT
import com.teamapi.palette.entity.tables.TblChat.TblChatPath.TBL_CHAT as CHAT_PATH

class ChatQueryRepositoryImpl(
    private val ctx: DSLContext
) : ChatQueryRepository {
    override suspend fun findPagedWithLastMessageId(
        roomId: Long,
        before: LocalDateTime,
        size: Long
    ): List<ChatResponse> {
        val dsl = ctx.dsl()
        return dsl
            .select(CHAT_PATH)
            .from(CHAT)
            .where(CHAT.ROOM_ID.eq(roomId).and(CHAT.DATETIME.lt(before)))
            .orderBy(CHAT.DATETIME.desc())
            .limit(size)
            .awaitAll { r ->
                r.value1().let {
                    ChatResponse(it.id!!, it.message, it.datetime, it.roomId, it.userId, it.isAi, it.resource)
                }
            }
    }
}
