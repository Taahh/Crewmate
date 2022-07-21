package dev.taah.crewmate.backend.event.room

import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.core.room.GameRoom

class GameRoomJoinEvent(val connection: PlayerConnection, val room: GameRoom) : IEvent {
}