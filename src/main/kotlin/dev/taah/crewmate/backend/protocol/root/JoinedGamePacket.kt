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

class JoinedGamePacket(nonce: Int) : AbstractPacket<JoinedGamePacket>(0x01, nonce) {
    var gameRoom: GameRoom? = null
    var joining: Int = 0
    override fun processPacket(packet: JoinedGamePacket, connection: PlayerConnection) {

    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x07)
        hazel.payload!!.writeInt32(gameRoom!!.gameCode.codeInt)
        hazel.payload!!.writeInt32(joining)
        hazel.payload!!.writeInt32(gameRoom!!.host)
        hazel.payload!!.writePackedInt32(gameRoom!!.players.size - 1)
        for ((k, v) in gameRoom!!.players.filter { entry -> entry.key != joining }) {
            hazel.payload!!.writePackedInt32(k)
            hazel.payload!!.writePackedString(v.clientName)
            v.platformData.serialize(hazel.payload!!)
            hazel.payload!!.writePackedInt32(0)
            hazel.payload!!.writePackedString("")
            hazel.payload!!.writePackedString("")
        }
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        println("Game Code Join: ${GameCode(buffer.readInt32()).codeString}")
        println("Crossplay Flags: ${buffer.readBoolean()}")
    }

}