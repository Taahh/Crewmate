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

class HostGamePacket(nonce: Int) : AbstractPacket<HostGamePacket>(0x01, nonce) {

    private var gameCode: GameCode? = null
    override fun processPacket(packet: HostGamePacket, connection: PlayerConnection) {
        val gameCode = GameCode.generateCode()
        val room = GameRoom(gameCode!!)
        GameRoom.ROOMS[gameCode!!] = room
        packet.gameCode = gameCode
        connection.sendReliablePacket(packet)
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x00)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        val gameOptionsData = GameOptionsData().deserialize(buffer)
        buffer.readInt32()
//        println("Crossplay Flags: ${buffer.readInt32()}")
    }

}