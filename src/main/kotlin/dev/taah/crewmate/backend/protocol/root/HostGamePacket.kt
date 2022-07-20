package dev.taah.crewmate.backend.protocol.root

import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode
import io.netty.buffer.ByteBufUtil

class HostGamePacket(nonce: Int) : AbstractPacket<HostGamePacket>(0x01, nonce) {
    override fun processPacket(packet: HostGamePacket, connection: PlayerConnection) {
        connection.sendReliablePacket(packet)
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x00)
        val gameCode = GameCode.generateCode()
        println("Game Code Host: ${gameCode.codeString}")
        hazel.payload!!.writeInt32(gameCode.codeInt)
        println("host buffer b4: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")

        hazel.endMessage()
        println("host buffer after: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        val gameOptionsData = GameOptionsData().deserialize(buffer)
        println("Data: ${CrewmateServer.GSON.toJson(gameOptionsData)}")
        println("Crossplay Flags: ${buffer.readInt32()}")
    }

}