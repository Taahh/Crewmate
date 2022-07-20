package dev.taah.crewmate.backend.protocol.root.reactor

import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBufUtil

class ReactorHandshakePacket(nonce: Int) : AbstractPacket<ReactorHandshakePacket>(0XFF.toByte(), nonce, ) {
    override fun processPacket(packet: ReactorHandshakePacket, connection: PlayerConnection) {
        TODO("Not yet implemented")

    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(this.packetType.toInt());

        hazel.payload!!.writeByte(0)
        hazel.payload!!.writePackedString("Crewmate")
        hazel.payload!!.writePackedString("0.1.1")
        hazel.payload!!.writePackedInt32(0)

        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        TODO("Not yet implemented")
    }
}