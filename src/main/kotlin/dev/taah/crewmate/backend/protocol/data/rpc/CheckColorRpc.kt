package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class CheckColorRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var bodyColor: Byte? = null

    constructor(bodyColor: Byte) : this() {
        this.bodyColor = bodyColor
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcCheckColor(this.bodyColor!!, room.host)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.CheckColor.id)
        buffer.writeByte(this.bodyColor!!.toInt())
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.bodyColor = buffer.readByte()
    }
}