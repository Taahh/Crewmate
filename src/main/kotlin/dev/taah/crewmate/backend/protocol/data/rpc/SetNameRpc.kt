package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetNameRpc() : AbstractMessage(0x00) {

    var name: String? = null

    constructor(name: String) : this() {
        this.name = name
    }

    override fun processObject(room: GameRoom) {
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetName.id)
        buffer.writePackedString(this.name!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.name = buffer.readPackedString()
    }

    fun name(name: String): SetNameRpc {
       this.name = name
        return this
    }
}