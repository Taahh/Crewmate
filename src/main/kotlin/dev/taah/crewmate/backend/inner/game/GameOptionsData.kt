package dev.taah.crewmate.backend.inner.game

import dev.taah.crewmate.api.inner.IGameOptionsData
import dev.taah.crewmate.api.inner.enums.TaskBarMode
import dev.taah.crewmate.backend.inner.roles.RoleOptionsData
import dev.taah.crewmate.util.PacketBuffer

class GameOptionsData : IGameOptionsData<RoleOptionsData> {
    override var version: Byte = 0
    override var maxPlayers: Byte = 0
    override var keywords: Int = 0
    override var map: Byte = 0
    override var speed: Float = 0.0f
    override var crewLight: Float = 0.0f
    override var imposterLight: Float = 0.0f
    override var killCooldown: Float = 0.0f
    override var commonTasks: Byte = 0
    override var longTasks: Byte = 0
    override var shortTasks: Byte = 0
    override var emergencyMeetings: Int = 0
    override var imposters: Byte = 0
    override var killDistance: Byte = 0
    override var discussionTime: Int = 0
    override var votingTime: Int = 0
    override var default: Boolean = false
    override var emergencyCooldowns: Byte = 0
    override var confirmEjects: Boolean = false
    override var visualTasks: Boolean = false
    override var anonymousVoting: Boolean = false
    override var taskbarMode: TaskBarMode = TaskBarMode.Normal
    override var roleOptionsData: RoleOptionsData = RoleOptionsData()

    override fun serialize(buffer: PacketBuffer) {
        var position = buffer.writerIndex()
        buffer.writePackedUInt32(0)
        buffer.writeByte(this.version.toInt())
        buffer.writeByte(this.maxPlayers.toInt())
        buffer.writeLong(this.keywords.toLong())
        buffer.writeByte(this.map.toInt())
        buffer.writeFloatLE(this.speed)
        buffer.writeFloatLE(this.crewLight)
        buffer.writeFloatLE(this.imposterLight)
        buffer.writeFloatLE(this.killCooldown)
        buffer.writeByte(this.commonTasks.toInt())
        buffer.writeByte(this.longTasks.toInt())
        buffer.writeByte(this.shortTasks.toInt())
        buffer.writeInt(this.emergencyMeetings)
        buffer.writeByte(this.imposters.toInt())
        buffer.writeByte(this.killDistance.toInt())
        buffer.writeInt(this.discussionTime)
        buffer.writeInt(this.votingTime)
        buffer.writeBoolean(this.default)

        if (this.version > 1) {
            buffer.writeByte(this.emergencyCooldowns.toInt())
        }
        if (this.version > 2) {
            buffer.writeBoolean(this.confirmEjects)
            buffer.writeBoolean(this.visualTasks)
        }
        if (this.version > 3) {
            buffer.writeBoolean(this.anonymousVoting)
            buffer.writeByte(this.taskbarMode.id)
        }
        if (this.version > 4) {
            this.roleOptionsData.serialize(buffer)
        }
        val endingPosition = buffer.writerIndex() - position
        buffer.markWriterIndex()
        buffer.writerIndex(position)
        buffer.writePackedUInt32(endingPosition.toLong())
        buffer.resetWriterIndex()
    }

    override fun deserialize(buffer: PacketBuffer): IGameOptionsData<RoleOptionsData> {
        buffer.readPackedUInt32()
        val gameOptionsData = GameOptionsData()
        gameOptionsData.version = buffer.readUnsignedByte().toByte()
        gameOptionsData.maxPlayers = buffer.readUnsignedByte().toByte()
        gameOptionsData.keywords = buffer.readUInt32().toInt()
        gameOptionsData.map = buffer.readUnsignedByte().toByte()
        gameOptionsData.speed = buffer.readFloatLE()
        gameOptionsData.crewLight = buffer.readFloatLE()
        gameOptionsData.imposterLight = buffer.readFloatLE()
        gameOptionsData.killCooldown = buffer.readFloatLE()
        gameOptionsData.commonTasks = buffer.readUnsignedByte().toByte()
        gameOptionsData.longTasks = buffer.readUnsignedByte().toByte()
        gameOptionsData.shortTasks = buffer.readUnsignedByte().toByte()
        gameOptionsData.emergencyMeetings = buffer.readUInt32().toInt()
        gameOptionsData.imposters = buffer.readUnsignedByte().toByte()
        gameOptionsData.killDistance = buffer.readUnsignedByte().toByte()
        gameOptionsData.discussionTime = buffer.readUInt32().toInt()
        gameOptionsData.votingTime = buffer.readUInt32().toInt()
        gameOptionsData.default = buffer.readBoolean()
        if (gameOptionsData.version > 1) {
            gameOptionsData.emergencyCooldowns = buffer.readUnsignedByte().toByte()
        }
        if (gameOptionsData.version > 2) {
            gameOptionsData.confirmEjects = buffer.readBoolean()
            gameOptionsData.visualTasks = buffer.readBoolean()
        }
        if (gameOptionsData.version > 3) {
            gameOptionsData.anonymousVoting = buffer.readBoolean()
            gameOptionsData.taskbarMode = TaskBarMode.getById(buffer.readUnsignedByte().toInt())!!
        }
        if (gameOptionsData.version > 4) {
            gameOptionsData.roleOptionsData = RoleOptionsData().deserialize(buffer) as RoleOptionsData
            println("set data")
        }
        return gameOptionsData
    }

}