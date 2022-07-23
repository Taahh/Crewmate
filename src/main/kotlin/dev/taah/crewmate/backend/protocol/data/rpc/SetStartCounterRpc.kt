package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SetStartCounterRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var num: Int? = null
    var secondsLeft: Byte? = null

    constructor(num: Int, secondsLeft: Byte) : this() {
        this.num = num
        this.secondsLeft = secondsLeft
    }

    override fun processObject(room: GameRoom) {
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetStartCounter(this.num!!, this.secondsLeft!!)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SetStartCounter.id)
        buffer.writePackedInt32(this.num!!)
        buffer.writeByte(this.secondsLeft!!.toInt())
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.num = buffer.readPackedInt32()
        this.secondsLeft = buffer.readByte()
    }
}