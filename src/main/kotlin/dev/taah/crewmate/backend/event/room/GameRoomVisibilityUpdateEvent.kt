package dev.taah.crewmate.backend.event.room

import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.api.inner.enums.GameVisibility
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.core.room.GameRoom

class GameRoomVisibilityUpdateEvent(val room: GameRoom, val old: GameVisibility, val new: GameVisibility) : IEvent {
}