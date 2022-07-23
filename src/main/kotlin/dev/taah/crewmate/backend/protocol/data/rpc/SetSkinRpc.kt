package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetSkinRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var skinId: String? = null

    constructor(skinId: String) : this() {
        this.skinId = skinId
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetSkin(this.skinId!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetSkin.id)
        buffer.writePackedString(this.skinId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.skinId = buffer.readPackedString()
    }
}