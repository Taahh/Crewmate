package dev.taah.crewmate.backend.protocol.root

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.api.inner.enums.GameVisibility
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.event.room.GameRoomVisibilityUpdateEvent
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode

class AlterGamePacket(nonce: Int) : AbstractPacket<AlterGamePacket>(0x01, nonce) {
    var gameCode: GameCode? = null
    var visibility: GameVisibility? = null
    override fun processPacket(packet: AlterGamePacket, connection: PlayerConnection) {
        if (gameCode != null) {
            if (GameRoom.exists(gameCode!!)) {
                var room = GameRoom.get(gameCode!!)
                val old = room.visibility
                room.visibility = visibility!!
                EventManager.INSTANCE!!.callEvent(GameRoomVisibilityUpdateEvent(room, old, room.visibility))
                room.broadcastReliablePacket(packet)
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(10)
        hazel.payload!!.writeInt32(this.gameCode!!.codeInt)
        hazel.payload!!.writeByte(1)
        hazel.payload!!.writeBoolean(visibility == GameVisibility.Public)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        buffer.readByte()
        this.visibility = if (buffer.readBoolean()) GameVisibility.Public else GameVisibility.Private
    }

}