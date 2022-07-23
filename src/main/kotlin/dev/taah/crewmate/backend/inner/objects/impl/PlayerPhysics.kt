package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class PlayerPhysics(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override var initialState: Boolean = false

    var new: Boolean = false
    var playerId: Byte = 0
    override fun processObject(room: GameRoom) {
        this.initialState = false
        room.connections[this.ownerId]!!.playerControl!!.playerPhysics = this
    }

    override fun serialize(buffer: PacketBuffer) {
    }

    override fun deserialize(hazelMessage: HazelMessage) {
    }
}