package dev.taah.crewmate.backend.protocol.option

import dev.taah.crewmate.api.inner.enums.QuickChatMode
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.data.PlatformData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.root.reactor.ReactorHandshakePacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.util.PacketBuffer

class HelloPacket(nonce: Int) : AbstractPacket<HelloPacket>(0x08, nonce) {
    var clientVersion: Int = 0
    var clientName: String = ""
    var lastLanguage: Int = 0
    var chatModeType: QuickChatMode? = null
    var platformData: PlatformData = PlatformData()

    override fun processPacket(packet: HelloPacket, connection: PlayerConnection) {
        connection.clientName = packet.clientName
        connection.clientVersion = packet.clientVersion
        connection.chatModeType = packet.chatModeType!!
        connection.platformData = packet.platformData
        connection.sendAck(this.nonce)
        val reliable = ReliablePacket(connection.getNextNonce())
        reliable.reliablePacket = ReactorHandshakePacket(reliable.nonce) as AbstractPacket<ReliablePacket>?
        connection.sendReliablePacket(reliable)
    }

    override fun serialize(buffer: PacketBuffer) {
//        TODO("Not yet implemented")
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.nonce = buffer.readUnsignedShort()
        buffer.readByte()
        this.clientVersion = buffer.readInt32()
        this.clientName = buffer.readPackedString()
        buffer.readUnsignedInt()
        this.lastLanguage = buffer.readUnsignedInt().toInt()
        this.chatModeType = QuickChatMode.getById(buffer.readByte().toInt())!!
        this.platformData = PlatformData().deserialize(buffer) as PlatformData
        buffer.readPackedString()
        buffer.readUnsignedInt()
    }
}