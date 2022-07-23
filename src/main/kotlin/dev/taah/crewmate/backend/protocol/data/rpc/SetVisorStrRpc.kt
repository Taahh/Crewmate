package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetVisorStrRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var visorId: String? = null

    constructor(visorId: String) : this() {
        this.visorId = visorId
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetVisorStr(this.visorId!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetVisorStr.id)
        buffer.writePackedString(this.visorId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.visorId = buffer.readPackedString()
    }
}