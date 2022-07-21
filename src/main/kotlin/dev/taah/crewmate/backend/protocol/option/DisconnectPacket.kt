package dev.taah.crewmate.backend.protocol.option

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.event.room.GameRoomLeaveEvent
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class DisconnectPacket(nonce: Int) : AbstractPacket<DisconnectPacket>(0x09, nonce) {

    var disconnectReasons: DisconnectReasons = DisconnectReasons.Custom
    var reason: String? = null

    override fun processPacket(packet: DisconnectPacket, connection: PlayerConnection) {
        if (connection.gameCode != null) {
            if (GameRoom.exists(connection.gameCode!!)) {
                val room = GameRoom.get(connection.gameCode!!)
                val player =
                    room.connections.entries.filter { entry -> entry.value.uniqueId.equals(connection.uniqueId) }.first()
                room.connections.remove(player.key)
                EventManager.INSTANCE!!.callEvent(GameRoomLeaveEvent(connection, room))
                if (room.connections.isEmpty()) {
                    CrewmateServer.LOGGER.info("Destroying room ${connection.gameCode!!.codeString}")
                    GameRoom.ROOMS.remove(GameRoom.ROOMS.entries.first { entry -> entry.key.equals(connection.gameCode!!) }.key)
                }
                connection.gameCode = null
            }
        }
        connection.channel.channel().attr(PlayerConnection.CONNECTION_STRING).set(null)
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