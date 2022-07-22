package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.data.rpc.SetNameRpc
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode

class PlayerControl(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override var initialState: Boolean = false

    var gameCode: GameCode? = null

    var playerPhysics: PlayerPhysics? = null
    var customNetworkTransform: CustomNetworkTransform? = null

    var new: Boolean = false
    var playerId: Byte = 0
    override fun processObject(room: GameRoom) {
        this.initialState = false
        room.connections[this.ownerId]!!.playerControl = this
        this.gameCode = room.connections[this.ownerId]!!.gameCode
    }

    override fun serialize(buffer: PacketBuffer) {
        if (this.initialState) {
            buffer.writeBoolean(this.new)
        }
        buffer.writeByte(this.playerId.toInt())
    }

    override fun deserialize(buffer: PacketBuffer) {
        if (this.initialState) {
            this.new = buffer.readBoolean()
        }
        this.playerId = buffer.readByte()
    }

    private fun checkRoom(): GameRoom? {
        if (this.gameCode == null) {
            return null
        }
        if (!GameRoom.exists(this.gameCode!!)) {
            return null
        }
        return GameRoom.get(this.gameCode!!)
    }

    fun rpcSetName(name: String) {
        val room: GameRoom = checkRoom() ?: return
        val rpc = SetNameRpc(name)
        val rpcMessage = RpcMessage(this.netId, rpc)
        room.broadcastReliablePacket(GameDataPacket(this.gameCode!!, rpcMessage))
    }
}