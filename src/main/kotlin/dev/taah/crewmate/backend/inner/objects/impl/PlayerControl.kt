package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class PlayerControl(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override var initialState: Boolean = false

    var playerPhysics: PlayerPhysics? = null
    var customNetworkTransform: CustomNetworkTransform? = null

    var new: Boolean = false
    var playerId: Byte = 0
    override fun processObject(room: GameRoom) {
//        println("player control: ${CrewmateServer.GSON.toJson(this)}")
        room.connections[this.ownerId]!!.playerInfo = room.gameData!!.players[this.playerId]
        println("set ${this.ownerId}'s player info and and player id to ${room.gameData!!.players[this.playerId]?.playerId}")
        room.connections[this.ownerId]!!.playerControl = this
    }

    override fun serialize(buffer: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun deserialize(buffer: PacketBuffer) {
        if (this.initialState) {
            this.new = buffer.readBoolean()
        }
        this.playerId = buffer.readByte()
    }
}