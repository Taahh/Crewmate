package dev.taah.crewmate.backend.protocol.root

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import dev.taah.crewmate.api.inner.enums.GameState
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.event.connection.GameRoomJoinEvent
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.DisconnectPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode

class JoinGamePacket(nonce: Int) : AbstractPacket<JoinGamePacket>(0x01, nonce) {
    var gameCode: GameCode? = null

    //
    var gameRoom: GameRoom? = null
    var joining: Int = 0
    override fun processPacket(packet: JoinGamePacket, connection: PlayerConnection) {
        var room: GameRoom
        if (!GameRoom.exists(gameCode!!)) {
            room = GameRoom(gameCode!!)
            GameRoom.ROOMS[gameCode!!] = room
        } else {
            room = GameRoom.get(gameCode!!)
        }

        if (room.state == GameState.InGame) {
            connection.sendDisconnect(DisconnectReasons.Custom, "Chris")
            return
        } else if (room.connections.values.any { it.uniqueId.equals(connection.uniqueId) }) {
            val player = room.connections.entries.filter { entry -> entry.value.uniqueId.equals(connection.uniqueId) }.first()
            if (room.state == GameState.WaitingForHost) {
                if (room.host == player.key) {
                    val packet = JoinedGamePacket(this.nonce)
                    packet.joining = room.host
                    packet.gameRoom = room
                    connection.sendReliablePacket(packet)
                    room.state = GameState.NotStarted
                    for (x in room.waitingForHost.toList()) {
                        val joinedGamePacket = JoinedGamePacket(this.nonce)
                        joinedGamePacket.joining = x
                        joinedGamePacket.gameRoom = room
                        room.connections[x]!!.sendReliablePacket(joinedGamePacket)
                        EventManager.INSTANCE!!.callEvent(GameRoomJoinEvent(connection, room))
                        room.waitingForHost.remove(x)
                    }
                } else {
                    val joinGamePacket = JoinGamePacket(this.nonce)
                    joinGamePacket.joining = player.key
                    joinGamePacket.gameRoom = room

                    room.broadcastReliablePacket(joinGamePacket, player.key)

                    val waitingForHostPacket = WaitingForHostPacket(this.nonce)
                    waitingForHostPacket.gameCode = room.gameCode
                    waitingForHostPacket.clientId = player.key
                    connection.sendReliablePacket(waitingForHostPacket)
                    room.waitingForHost.add(player.key)
                }
            } else if (room.state == GameState.NotStarted) {
                val joinGamePacket = JoinGamePacket(this.nonce)
                joinGamePacket.joining = player.key
                joinGamePacket.gameRoom = room
                room.broadcastReliablePacket(joinGamePacket, player.key)

                val joinedGamePacket = JoinedGamePacket(this.nonce)
                joinedGamePacket.joining = player.key
                joinedGamePacket.gameRoom = room
                connection.sendReliablePacket(joinedGamePacket)
                EventManager.INSTANCE!!.callEvent(GameRoomJoinEvent(connection, room))
            }
            return
        }
        var id = room.connections.size + 1
        if (room.host == -1) {
            if (room.connections.isEmpty()) {
                room.host = id
            } else {
                room.host = room.connections.keys.first()
            }
        }
        room.connections[id] = connection
        CrewmateServer.LOGGER.info("[Room ${room.gameCode.codeString}] Assigning player ID $id to ${connection.clientName}")

        val packet = JoinedGamePacket(this.nonce)
        packet.joining = id
        packet.gameRoom = room

        val joinGamePacket = JoinGamePacket(this.nonce)
        joinGamePacket.joining = id
        joinGamePacket.gameRoom = room

        connection.gameCode = gameCode

        if (room.host != id) {
            room.broadcastReliablePacket(joinGamePacket, id)
        }
        connection.sendReliablePacket(packet)
        EventManager.INSTANCE!!.callEvent(GameRoomJoinEvent(connection, room))
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x01)
        hazel.payload!!.writeInt32(this.gameRoom!!.gameCode!!.codeInt)
        hazel.payload!!.writeInt32(this.joining)
        hazel.payload!!.writeInt32(this.gameRoom!!.host)
        val room = this.gameRoom!!
        val player = room.connections[joining]!!
        hazel.payload!!.writePackedString(player.clientName)
        player.platformData.serialize(hazel.payload!!)
        hazel.payload!!.writePackedInt32(0)
        hazel.payload!!.writePackedString("")
        hazel.payload!!.writePackedString("")
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameCode = GameCode(buffer.readInt32())
        buffer.readBoolean()
//        println("Crossplay Flags: ${buffer.readBoolean()}")
    }

}