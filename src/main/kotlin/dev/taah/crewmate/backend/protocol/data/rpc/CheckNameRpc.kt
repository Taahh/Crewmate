package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class CheckNameRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var name: String? = null

    constructor(name: String) : this() {
        this.name = name
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetName(this.name!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.CheckName.id)
        buffer.writePackedString(this.name!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.name = buffer.readPackedString()
    }

    fun name(name: String): CheckNameRpc {
       this.name = name
        return this
    }
}