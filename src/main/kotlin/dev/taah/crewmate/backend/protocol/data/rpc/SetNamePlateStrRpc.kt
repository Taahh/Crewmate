package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetNamePlateStrRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var namePlateId: String? = null

    constructor(namePlateId: String) : this() {
        this.namePlateId = namePlateId
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetNamePlateStr(this.namePlateId!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetNamePlateStr.id)
        buffer.writePackedString(this.namePlateId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.namePlateId = buffer.readPackedString()
    }
}