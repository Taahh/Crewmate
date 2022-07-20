package dev.taah.crewmate.backend.protocol.root

import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode
import io.netty.buffer.ByteBufUtil

class GameDataToPacket(nonce: Int) : AbstractPacket<GameDataToPacket>(0x01, nonce) {

    var target: Int = 0
    var gameCode: GameCode? = null
    var buffer: PacketBuffer? = null

    override fun processPacket(packet: GameDataToPacket, connection: PlayerConnection) {
//        connection.sendReliablePacket(packet)
        if (gameCode != null) {
            println("GAME ROOM EXISTS? ${GameRoom.exists(gameCode!!)}")
            if (GameRoom.exists(gameCode!!)) {
                var room = GameRoom.get(gameCode!!)
                room.players[packet.target]!!.sendReliablePacket(packet)
            }
        }
        println("GAME DATA CODE: ${gameCode!!.codeString}")
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x06)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.payload!!.writePackedInt32(this.target)
        hazel.payload!!.writeBytes(this.buffer!!.copyPacketBuffer())
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        this.target = buffer.readPackedInt32();
        this.buffer = buffer.copyPacketBuffer()
        println("GAME DATA TO: ${ByteBufUtil.prettyHexDump(buffer)}")
    }

}