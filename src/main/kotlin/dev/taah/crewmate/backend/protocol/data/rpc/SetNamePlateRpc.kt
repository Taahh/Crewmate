package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetNamePlateRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var namePlateId: String? = null

    constructor(namePlateId: String) : this() {
        this.namePlateId = namePlateId
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetNamePlate(this.namePlateId!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetNamePlate.id)
        buffer.writePackedString(this.namePlateId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.namePlateId = buffer.readPackedString()
    }
}