package dev.taah.crewmate.backend.protocol.option

import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.util.inner.GameDataUtil
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode
import io.netty.buffer.ByteBufUtil

class NormalPacket(nonce: Int) : AbstractPacket<NormalPacket>(0x00, nonce) {

    var buffer: PacketBuffer? = null

    override fun processPacket(packet: NormalPacket, connection: PlayerConnection) {
        if (connection.gameCode == null) return
        if (!GameRoom.exists(connection.gameCode!!)) return
        val room = GameRoom.get(connection.gameCode!!)
        room.broadcastPacket(packet)
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeBytes(this.buffer!!.copyPacketBuffer())
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.buffer = buffer.copyPacketBuffer()
        val hazel = HazelMessage.read(buffer)!!
        val gameCode = GameCode(hazel.payload!!.readInt32())
//        println("game code: ${gameCode.codeString}")
//        println("buffer: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")
        GameDataUtil.handleGameData(hazel.payload!!, GameRoom.get(gameCode))
    }
}