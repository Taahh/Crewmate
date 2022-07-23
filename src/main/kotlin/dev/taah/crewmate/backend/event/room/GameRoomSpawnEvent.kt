package dev.taah.crewmate.backend.event.room

import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom

class GameRoomSpawnEvent(val room: GameRoom, val spawnId: Int, val innerNetObjects: List<AbstractInnerNetObject>) : IEvent {
}