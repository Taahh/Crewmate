package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetLevelRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var level: Int? = null

    constructor(level: Int) : this() {
        this.level = level
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetLevel(this.level!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetLevel.id)
        buffer.writePackedUInt32(this.level!!.toLong())
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.level = buffer.readPackedUInt32().toInt()
    }
}