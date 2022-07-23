package dev.taah.crewmate.backend.protocol.root

import dev.taah.crewmate.api.inner.enums.DisconnectReasons
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

class RemovePlayerPacket(nonce: Int) : AbstractPacket<RemovePlayerPacket>(0x01, nonce) {
    var gameCode: GameCode? = null
    var targetId: Int? = null
    var disconnectReasons: DisconnectReasons? = null

    constructor(gameCode: GameCode, targetId: Int, disconnectReasons: DisconnectReasons): this(-1) {
        this.gameCode = gameCode
        this.targetId = targetId
        this.disconnectReasons = disconnectReasons
    }

    override fun processPacket(packet: RemovePlayerPacket, connection: PlayerConnection) {
        if (gameCode != null) {
            if (GameRoom.exists(gameCode!!)) {
                var room = GameRoom.get(gameCode!!)
                room.state = GameState.WaitingForHost
//                room.broadcastReliablePacket(packet)
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x04)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.payload!!.writePackedInt32(this.targetId!!)
        hazel.payload!!.writeByte(this.disconnectReasons!!.id)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        this.targetId = buffer.readPackedInt32()
        this.disconnectReasons = DisconnectReasons.getById(buffer.readUnsignedByte().toInt())
    }

}