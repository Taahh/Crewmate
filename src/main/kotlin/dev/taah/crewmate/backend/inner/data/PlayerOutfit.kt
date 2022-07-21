package dev.taah.crewmate.backend.inner.data

import dev.taah.crewmate.api.inner.IPlayerOutfit
import dev.taah.crewmate.util.PacketBuffer

class PlayerOutfit : IPlayerOutfit {
    override var colorId: Int = 0
    override var hatId: String = ""
    override var petId: String = ""
    override var skinId: String = ""
    override var visorId: String = ""
    override var namePlateId: String = ""
    override var preCensorName: String = ""

    override fun serialize(buffer: PacketBuffer) {
        buffer.writePackedString(this.preCensorName)
        buffer.writePackedInt32(this.colorId)
        buffer.writePackedString(this.hatId)
        buffer.writePackedString(this.petId)
        buffer.writePackedString(this.skinId)
        buffer.writePackedString(this.visorId)
        buffer.writePackedString(this.namePlateId)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.preCensorName = buffer.readPackedString()
        this.colorId = buffer.readPackedInt32()
        this.hatId = buffer.readPackedString()
        this.petId = buffer.readPackedString()
        this.skinId = buffer.readPackedString()
        this.visorId = buffer.readPackedString()
        this.namePlateId = buffer.readPackedString()
    }
}