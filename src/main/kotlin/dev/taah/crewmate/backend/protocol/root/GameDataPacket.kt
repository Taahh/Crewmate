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

class GameDataPacket(nonce: Int) : AbstractPacket<GameDataPacket>(0x01, nonce) {

    var gameCode: GameCode? = null
    var buffer: PacketBuffer? = null

    override fun processPacket(packet: GameDataPacket, connection: PlayerConnection) {
        if (gameCode != null) {
            if (GameRoom.exists(gameCode!!)) {
                var room = GameRoom.get(gameCode!!)
                var id = room.players.entries.filter { entry -> entry.value.uniqueId.equals(connection.uniqueId) }.map { mutableEntry -> mutableEntry.key }.first()
                room.broadcastReliablePacket(packet, id)
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x05)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.payload!!.writeBytes(this.buffer!!.copyPacketBuffer())
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        this.buffer = buffer.copyPacketBuffer()
    }

}