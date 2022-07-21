package dev.taah.crewmate.backend.inner.data

import dev.taah.crewmate.api.inner.IPlayerOutfit
import dev.taah.crewmate.api.inner.ITaskInfo
import dev.taah.crewmate.util.PacketBuffer

class TaskInfo : ITaskInfo {
    override var id: Int = 0
    override var typeId: Byte = 0
    override var complete: Boolean = false

    override fun serialize(buffer: PacketBuffer) {
        buffer.writePackedUInt32(this.id.toLong())
        buffer.writeBoolean(this.complete)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.id = buffer.readPackedUInt32().toInt()
        this.complete = buffer.readBoolean()
    }
}