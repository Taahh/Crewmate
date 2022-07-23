package dev.taah.crewmate.backend.protocol.data

import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.core.room.GameRoom

abstract class AbstractMessage : ISerializable, IDeserializable<Unit> {
    var target: Int? = null
    var sender: PlayerConnection? = null

    abstract fun processObject(room: GameRoom)
}