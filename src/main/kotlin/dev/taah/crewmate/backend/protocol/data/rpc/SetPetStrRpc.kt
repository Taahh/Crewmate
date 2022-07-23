package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetPetStrRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var petId: String? = null

    constructor(petId: String) : this() {
        this.petId = petId
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetPetStr(this.petId!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetPetStr.id)
        buffer.writePackedString(this.petId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.petId = buffer.readPackedString()
    }
}