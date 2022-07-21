package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class PlayerPhysics(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override var initialState: Boolean = false

    var new: Boolean = false
    var playerId: Byte = 0
    override fun processObject(room: GameRoom) {
        room.connections[this.ownerId]!!.playerControl!!.playerPhysics = this
    }

    override fun serialize(buffer: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun deserialize(buffer: PacketBuffer) {
    }
}