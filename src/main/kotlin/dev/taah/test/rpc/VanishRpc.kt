package dev.taah.test.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class VanishRpc() : AbstractMessage(0x00) {

    var vanish: Boolean = false

    constructor(vanish: Boolean) : this() {
        this.vanish = vanish
    }


    override fun deserialize(buffer: PacketBuffer) {
        this.vanish = buffer.readBoolean()
    }

    override fun processObject(room: GameRoom) {
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(234)
        buffer.writeBoolean(this.vanish)
    }
}