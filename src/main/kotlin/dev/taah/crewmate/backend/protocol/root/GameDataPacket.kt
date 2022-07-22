package dev.taah.crewmate.backend.protocol.root

import com.google.common.collect.Lists
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.backend.util.inner.GameDataUtil
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode
import io.netty.buffer.ByteBufUtil

class GameDataPacket(nonce: Int = -1) : AbstractPacket<GameDataPacket>(0x01, nonce) {

    var gameCode: GameCode? = null
    var buffer: PacketBuffer? = null

    val messages: ArrayList<AbstractMessage> = Lists.newArrayList()

    constructor(gameCode: GameCode, vararg messages: AbstractMessage): this() {
        this.gameCode = gameCode
        this.messages.addAll(messages.toList())
    }

    override fun processPacket(packet: GameDataPacket, connection: PlayerConnection) {
        if (gameCode != null) {
            if (GameRoom.exists(gameCode!!)) {
                var room = GameRoom.get(gameCode!!)
                var id = room.connections.entries.filter { entry -> entry.value.uniqueId.equals(connection.uniqueId) }.map { mutableEntry -> mutableEntry.key }.first()
                room.broadcastReliablePacket(packet, id)
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x05)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        if (this.buffer != null) {
            hazel.payload!!.writeBytes(this.buffer!!.copyPacketBuffer())
        }
        messages.forEach { it.serialize(hazel.payload!!) }
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        this.buffer = buffer.copyPacketBuffer()

        GameDataUtil.handleGameData(buffer, GameRoom.get(this.gameCode!!))
    }

    fun addMessage(message: AbstractMessage): GameDataPacket {
        this.messages.add(message)
        return this
    }
    fun gameCode(gameCode: GameCode): GameDataPacket {
        this.gameCode = gameCode
        return this
    }

}