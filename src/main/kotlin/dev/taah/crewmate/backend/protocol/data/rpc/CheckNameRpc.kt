package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class CheckNameRpc : AbstractMessage(0x00) {

    var targetNetId: Int = 0

    override fun processObject(room: GameRoom) {
    }

    override fun serialize(buffer: PacketBuffer) {
    }

    override fun deserialize(buffer: PacketBuffer) {
        buffer.readPackedString()
    }
}