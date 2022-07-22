package dev.taah.crewmate.backend.event.game

import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom

class GameRoomVentEvent(val room: GameRoom, val ventId: Int, val connection: PlayerConnection, val enter: Boolean) : IEvent {
}