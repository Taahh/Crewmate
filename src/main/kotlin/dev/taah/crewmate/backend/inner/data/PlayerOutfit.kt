package dev.taah.crewmate.backend.inner.data

import dev.taah.crewmate.api.inner.IPlayerOutfit
import dev.taah.crewmate.util.PacketBuffer

class PlayerOutfit : IPlayerOutfit {
    override var colorId: Int = 0
    override var hatId: String = "missing"
    override var petId: String = "missing"
    override var skinId: String = "missing"
    override var visorId: String = "missing"
    override var namePlateId: String = "missing"
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
        if (this.hatId.isEmpty()) {
            this.hatId = "missing"
        }
        this.petId = buffer.readPackedString()
        if (this.petId.isEmpty()) {
            this.petId = "missing"
        }
        this.skinId = buffer.readPackedString()
        if (this.skinId.isEmpty()) {
            this.skinId = "missing"
        }
        this.visorId = buffer.readPackedString()
        if (this.visorId.isEmpty()) {
            this.visorId = "missing"
        }
        this.namePlateId = buffer.readPackedString()
        if (this.namePlateId.isEmpty()) {
            this.namePlateId = "missing"
        }
    }
}