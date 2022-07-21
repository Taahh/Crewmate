package dev.taah.crewmate.backend.event.room

import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.InnerNetObjects
import dev.taah.crewmate.core.room.GameRoom

class GameRoomSpawnEvent(val room: GameRoom, val type: InnerNetObjects, val innerNetObject: AbstractInnerNetObject) : IEvent {
}