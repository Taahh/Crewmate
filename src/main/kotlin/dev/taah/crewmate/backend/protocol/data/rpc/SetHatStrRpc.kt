package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetHatStrRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var hatId: String? = null

    constructor(hatId: String) : this() {
        this.hatId = hatId
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetHatStr(this.hatId!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetHatStr.id)
        buffer.writePackedString(this.hatId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.hatId = buffer.readPackedString()
    }
}