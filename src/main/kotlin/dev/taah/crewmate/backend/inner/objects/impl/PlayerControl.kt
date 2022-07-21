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
}