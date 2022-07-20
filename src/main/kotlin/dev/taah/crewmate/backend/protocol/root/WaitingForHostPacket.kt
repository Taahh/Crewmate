package dev.taah.crewmate.backend.protocol.root

import dev.taah.crewmate.api.inner.enums.GameState
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

class WaitingForHostPacket(nonce: Int) : AbstractPacket<WaitingForHostPacket>(0x01, nonce) {
    var gameCode: GameCode? = null
    var clientId: Int = 0
    override fun processPacket(packet: WaitingForHostPacket, connection: PlayerConnection) {
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(12)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.payload!!.writeInt32(this.clientId)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {

    }

}