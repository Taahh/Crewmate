package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class PlayerControl(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override fun processObject(room: GameRoom) {
    }

    override fun serialize(buffer: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun deserialize(buffer: PacketBuffer, room: GameRoom) {

    }
}