package com.teamapi.palette.ws

import com.teamapi.palette.ws.actor.SinkActor
import com.teamapi.palette.ws.actor.SinkMessages
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SinkListenerDisposer(
    private val sinkActor: SinkActor
) {
    @Scheduled(cron = "* */5 * * * *")
    suspend fun removeDisposable() {
        sinkActor.send(SinkMessages.CleanUp)
    }
}
