package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetColorRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var bodyColor: Byte? = null

    constructor(bodyColor: Byte) : this() {
        this.bodyColor = bodyColor
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetColor(this.bodyColor!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetColor.id)
        buffer.writeByte(this.bodyColor!!.toInt())
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.bodyColor = buffer.readByte()
    }
}