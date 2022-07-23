package dev.taah.crewmate.backend.inner.data

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.api.inner.IPlayerInfo
import dev.taah.crewmate.api.inner.enums.PlayerOutfitType
import dev.taah.crewmate.api.inner.enums.RoleType
import dev.taah.crewmate.util.PacketBuffer

class PlayerInfo : IPlayerInfo {
    override val outfits: HashMap<PlayerOutfitType, PlayerOutfit> = Maps.newHashMap()
    override var playerId: Byte = 0
    override var tasks: ArrayList<TaskInfo> = Lists.newArrayList()
    override var level: Int = 0
    override var disconnected: Boolean = false
    override var dead: Boolean = false
    override var roleType: RoleType = RoleType.Crewmate
    override var friendCode: String = ""
    override var puid: String = ""

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(this.outfits.size)
        for ((k, v) in this.outfits) {
            buffer.writeByte(k.id)
            v.serialize(buffer)
        }
        buffer.writePackedUInt32(this.level.toLong())
        var num = 0
        if (this.disconnected) {
            num = num or 1
        }
        if (this.dead) {
            num = num or 4
        }
        buffer.writeByte(num)
        buffer.writeUInt16(this.roleType.id)
        buffer.writeByte(this.tasks.size)
        for (x in tasks) {
            x.serialize(buffer)
        }
        buffer.writePackedString(friendCode)
        buffer.writePackedString(puid)
    }

    override fun deserialize(buffer: PacketBuffer) {
        val count = buffer.readByte();
        for (i in 0 until count) {
            val type = PlayerOutfitType.getById(buffer.readByte().toInt())
            val outfit = PlayerOutfit()
            outfit.deserialize(buffer)
//            println("outfit: ${CrewmateServer.GSON.toJson(outfit)}")
            this.outfits[type!!] = outfit
        }
        this.level = buffer.readPackedUInt32().toInt()
        val flags = buffer.readByte().toUInt()
        this.disconnected = (flags.and(1U)) > 0U
        this.dead = (flags.and(4U)) > 0U
        this.roleType = RoleType.getById(buffer.readUInt16())!!
        val tasks = buffer.readByte()
        for (i in 0 until tasks) {
            val task = TaskInfo()
            task.deserialize(buffer)
            this.tasks.add(task)
        }
        this.friendCode = buffer.readPackedString()
        this.puid = buffer.readPackedString()
    }
}