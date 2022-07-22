package dev.taah.crewmate.backend.event.room

import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom

class GameRoomDataEvent(val room: GameRoom, val innerNetObject: AbstractInnerNetObject) : IEvent {
}