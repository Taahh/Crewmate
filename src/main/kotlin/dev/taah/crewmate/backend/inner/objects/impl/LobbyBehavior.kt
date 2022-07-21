package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class LobbyBehavior(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override var initialState: Boolean = false

    override fun processObject(room: GameRoom) {
        this.initialState = false
        room.spawnedObjects[this.netId] = this
    }

    override fun serialize(buffer: PacketBuffer) {
    }

    override fun deserialize(buffer: PacketBuffer) {
    }
}