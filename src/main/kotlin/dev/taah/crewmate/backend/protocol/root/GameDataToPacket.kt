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
import java.util.function.Consumer

class GameDataToPacket(nonce: Int = -1) : AbstractPacket<GameDataToPacket>(0x01, nonce) {

    var target: Int = 0
    var gameCode: GameCode? = null
    var buffer: PacketBuffer? = null

    val messages: ArrayList<AbstractMessage> = Lists.newArrayList()

    override fun processPacket(packet: GameDataToPacket, connection: PlayerConnection) {
        if (gameCode != null) {
            if (GameRoom.exists(gameCode!!)) {
                var room = GameRoom.get(gameCode!!)
                room.connections[target]!!.sendReliablePacket(packet)
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x06)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.payload!!.writePackedInt32(this.target)
        if (this.buffer != null) {
            hazel.payload!!.writeBytes(this.buffer!!.copyPacketBuffer())
        }
        messages.forEach { it.serialize(hazel.payload!!) }
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        this.target = buffer.readPackedInt32()
        this.buffer = buffer.copyPacketBuffer()

        GameDataUtil.handleGameData(buffer, GameRoom.get(this.gameCode!!))
    }

    fun addMessage(message: AbstractMessage): GameDataToPacket {
        this.messages.add(message)
        return this
    }
    fun gameCode(gameCode: GameCode): GameDataToPacket {
        this.gameCode = gameCode
        return this
    }

    fun target(targetNetId: Int): GameDataToPacket {
        this.target = targetNetId
        return this
    }

}