package dev.taah.crewmate.backend.protocol.option

import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.util.PacketBuffer

class AcknowledgementPacket(nonce: Int) : AbstractPacket<AcknowledgementPacket>(0x0a, nonce) {

    override fun processPacket(packet: AcknowledgementPacket, connection: PlayerConnection) {
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeShort(this.nonce)
        buffer.writeByte(0xff)
    }

    override fun deserialize(buffer: PacketBuffer) {
        
    }
}