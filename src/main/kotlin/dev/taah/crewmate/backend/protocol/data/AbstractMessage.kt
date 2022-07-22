package dev.taah.crewmate.backend.protocol.data

import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable
import dev.taah.crewmate.core.room.GameRoom

abstract class AbstractMessage(val flag: Int) : ISerializable, IDeserializable<Unit> {

    abstract fun processObject(room: GameRoom)
}