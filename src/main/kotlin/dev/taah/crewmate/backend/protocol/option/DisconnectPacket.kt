package dev.taah.crewmate.backend.protocol.option

import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class DisconnectPacket(nonce: Int) : AbstractPacket<DisconnectPacket>(0x09, nonce) {

    var disconnectReasons: DisconnectReasons = DisconnectReasons.Custom
    var reason: String? = null

    override fun processPacket(packet: DisconnectPacket, connection: PlayerConnection) {

    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(1)
        val hazel = HazelMessage.start(0x00)
        hazel.payload!!.writeByte(this.disconnectReasons.id)
        if (this.reason != null) {
            hazel.payload!!.writePackedString(this.reason!!)
        }
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
    }
}