package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.room.IRoom
import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.data.PlayerInfo
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.util.PacketBuffer

interface IInnerNetObject<G: IRoom<*, *, *>> {
    val netId: Int
    val ownerId: Int
    fun deserialize(buffer: PacketBuffer, room: G)
    fun serialize(buffer: PacketBuffer)
    fun processObject(room: G)
}