package com.teamapi.palette.repository

import com.teamapi.palette.dto.room.RoomResponse
import com.teamapi.palette.entity.Room
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import com.teamapi.palette.entity.tables.TblChat.TBL_CHAT as CHAT
import com.teamapi.palette.entity.tables.TblChat.TblChatPath.TBL_CHAT as CHAT_PATH
import com.teamapi.palette.entity.tables.TblRoom.TBL_ROOM as ROOM
import com.teamapi.palette.entity.tables.TblRoom.TblRoomPath.TBL_ROOM as ROOM_PATH

@Repository
interface RoomRepository : CoroutineCrudRepository<Room, Long>, RoomQueryRepository

interface RoomQueryRepository {
    suspend fun findRoomByUserId(userId: Long): List<RoomResponse>
}

@Repository
class RoomQueryRepositoryImpl(
    private val ctx: DSLContext
) : RoomQueryRepository {
    override suspend fun findRoomByUserId(userId: Long): List<RoomResponse> {
        val dsl = ctx.dsl()
        return dsl
            .select(
                ROOM_PATH,
                DSL.field(
                    dsl
                        .select(CHAT_PATH)
                        .from(CHAT)
                        .where(
                            CHAT.ROOM_ID.eq(ROOM.ID)
                                .and(CHAT.IS_AI.eq(true))
                                .and(CHAT.RESOURCE.eq("CHAT"))
                        )
                        .orderBy(CHAT.ID.desc())
                        .limit(1)
                )
            )
            .from(ROOM)
            .where(ROOM_PATH.USER_ID.eq(userId))
            .awaitAll { RoomResponse(it.value1().id, it.value1().title, it.value2()?.message) }
    }
}
