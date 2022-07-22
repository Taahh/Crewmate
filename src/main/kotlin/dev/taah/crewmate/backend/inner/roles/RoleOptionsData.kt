package dev.taah.crewmate.backend.inner.roles

import com.google.common.collect.Maps
import dev.taah.crewmate.api.inner.IRoleOptionsData
import dev.taah.crewmate.api.inner.enums.RoleType
import dev.taah.crewmate.util.PacketBuffer
import java.util.function.BiConsumer

class RoleOptionsData(
) : IRoleOptionsData<RoleRate> {
    override val roleRates: HashMap<RoleType, RoleRate> = Maps.newHashMap()
    override var shapeshifterLeaveSkin: Boolean = false
    override var shapeshifterCooldown: Byte = 0
    override var shapeshifterDuration: Byte = 0
    override var scientistCooldown: Byte = 0
    override var guardianAngelCooldown: Byte = 0
    override var engineerCooldown: Byte = 0
    override var engineerVentMaxTime: Byte = 0
    override var scientistBatteryCharge: Byte = 0
    override var protectionDuration: Byte = 0
    override var impostersSeeProtected: Boolean = false


    override fun serialize(buffer: PacketBuffer) {
        buffer.writePackedInt32(this.roleRates.size)
        for ((k, v) in this.roleRates) {
            buffer.writeShort(k.id)
            buffer.writeByte(v.maxCount)
            buffer.writeByte(v.chance)
        }
        buffer.writeBoolean(this.shapeshifterLeaveSkin);
        buffer.writeByte(this.shapeshifterCooldown.toInt());
        buffer.writeByte(this.shapeshifterDuration.toInt());
        buffer.writeByte(this.scientistCooldown.toInt());
        buffer.writeByte(this.guardianAngelCooldown.toInt());
        buffer.writeByte(this.engineerCooldown.toInt());
        buffer.writeByte(this.engineerVentMaxTime.toInt());
        buffer.writeByte(this.scientistBatteryCharge.toInt());
        buffer.writeByte(this.protectionDuration.toInt());
        buffer.writeBoolean(this.impostersSeeProtected);
    }

    override fun deserialize(buffer: PacketBuffer): IRoleOptionsData<RoleRate> {
        val roleOptionsData = RoleOptionsData()
        val num = buffer.readInt32()
        for (i in 0..num) {
            val roleType = buffer.readUInt16()
            val type = RoleType.getById(roleType)
            if (type == null) {
                buffer.readerIndex(buffer.readerIndex() - 2)
                continue
            }
            val rate = RoleRate(
                buffer.readUnsignedByte().toInt(),
                buffer.readUnsignedByte().toInt(),
            )
            roleOptionsData.roleRates[type] = rate;
        }
        roleOptionsData.shapeshifterLeaveSkin = buffer.readBoolean()
        roleOptionsData.shapeshifterCooldown = buffer.readUnsignedByte().toByte()
        roleOptionsData.shapeshifterDuration = buffer.readUnsignedByte().toByte()
        roleOptionsData.scientistCooldown = buffer.readUnsignedByte().toByte()
        roleOptionsData.guardianAngelCooldown = buffer.readUnsignedByte().toByte()
        roleOptionsData.engineerCooldown = buffer.readUnsignedByte().toByte()
        roleOptionsData.engineerVentMaxTime = buffer.readUnsignedByte().toByte()
        roleOptionsData.scientistBatteryCharge = buffer.readUnsignedByte().toByte()
        roleOptionsData.protectionDuration = buffer.readUnsignedByte().toByte()
        roleOptionsData.impostersSeeProtected = buffer.readBoolean()
        return roleOptionsData
    }
}