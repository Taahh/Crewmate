package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetNameRpc : AbstractMessage(0x00) {

    var name: String? = null

    override fun processObject(room: GameRoom) {
        val rpcMessage = RpcMessage().rpc(this).target(room.connections[room.host]!!.playerControl!!.netId)
        room.broadcastReliablePacket(GameDataPacket(-1).addMessage(rpcMessage).gameCode(room.gameCode))
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