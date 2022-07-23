package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.data.rpc.*
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.backend.protocol.root.GameDataToPacket
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
        this.gameCode = room.connections[this.ownerId]!!.gameCode
        if (this.initialState) {
//            CrewmateServer.LOGGER.debug("PLAYER CONTROL SETTING CLIENT NAME for OWNER ID ${this.ownerId}")
//            this.rpcSetName(room.getConnectionByClientId(this.ownerId)!!.clientName)
            if (room.host != this.ownerId) {
                val conn = room.getConnectionByClientId(this.ownerId)
                CrewmateServer.LOGGER.debug("connection null? ${conn != null}")
//                CrewmateServer.LOGGER.debug("room host has player control? ")
//                this.rpcCheckName(conn!!.clientName, room.host)
            }
        }
        this.initialState = false
        room.connections[this.ownerId]!!.playerControl = this
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

    fun sendRpc(rpc: AbstractMessage, targetClientId: Int? = null) {
        val room: GameRoom = checkRoom() ?: return
        val rpcMessage = RpcMessage(this.netId, rpc)
        if (targetClientId == null) {
            room.broadcastReliablePacket(GameDataPacket(this.gameCode!!, rpcMessage), room.connections.entries.first {  it.value.uniqueId.equals(room.getConnectionByPlayerControlNetId(this.netId)!!.uniqueId) }.key)
        } else {
            room.connections[targetClientId]!!.sendReliablePacket(GameDataToPacket().gameCode(this.gameCode!!).addMessage(rpcMessage).target(targetClientId))
        }
    }

    fun rpcSetName(name: String, targetClientId: Int? = null) {
        sendRpc(SetNameRpc(name), targetClientId)
    }

    fun rpcSetNamePlateStr(namePlateId: String, targetClientId: Int? = null) {
        sendRpc(SetNamePlateStrRpc(namePlateId), targetClientId)
    }

    fun rpcSetColor(bodyColor: Byte, targetClientId: Int? = null) {
        sendRpc(SetColorRpc(bodyColor), targetClientId)
    }

    fun rpcCheckColor(bodyColor: Byte, targetClientId: Int? = null) {
        sendRpc(CheckColorRpc(bodyColor), targetClientId)
    }

    fun rpcSetPetStr(petId: String, targetClientId: Int? = null) {
        sendRpc(SetPetStrRpc(petId), targetClientId)
    }

    fun rpcSetHatStr(hatId: String, targetClientId: Int? = null) {
        sendRpc(SetHatStrRpc(hatId), targetClientId)
    }

    fun rpcSetSkinStr(hatId: String, targetClientId: Int? = null) {
        sendRpc(SetSkinStrRpc(hatId), targetClientId)
    }

    fun rpcSetVisorStr(visorId: String, targetClientId: Int? = null) {
        sendRpc(SetVisorStrRpc(visorId), targetClientId)
    }

    fun rpcSetStartCounter(num: Int, secondsLeft: Byte, targetClientId: Int? = null) {
        sendRpc(SetStartCounterRpc(num, secondsLeft), targetClientId)
    }

    fun rpcSetLevel(level: Int, targetClientId: Int? = null) {
        sendRpc(SetLevelRpc(level), targetClientId)
    }

    fun rpcCheckName(name: String, targetClientId: Int? = null) {
        println("sending check name")
        sendRpc(CheckNameRpc(name), targetClientId)
    }

    fun rpcSendChat(message: String) {
        sendRpc(SendChatRpc(message))
    }
}