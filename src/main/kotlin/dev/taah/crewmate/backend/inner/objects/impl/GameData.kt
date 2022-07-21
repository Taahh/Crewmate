package dev.taah.crewmate.backend.inner.objects.impl

import com.google.common.collect.Maps
import dev.taah.crewmate.backend.inner.data.PlayerInfo
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class GameData(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    val players: HashMap<Byte, PlayerInfo> = Maps.newHashMap()
    override var initialState: Boolean = false
    override fun processObject(room: GameRoom) {
        for (x in players.values) {
            room.getConnectionByPlayerId(x.playerId)!!.playerInfo = x
        }
        room.spawnedObjects[this.netId] = this
    }

    override fun serialize(buffer: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun deserialize(buffer: PacketBuffer) {
        while (buffer.readableBytes() > 0) {
            try {
                val hazel = HazelMessage.read(buffer) ?: break
//                println("other buffer: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")
                var playerInfo = players[hazel.getTag().toByte()]
                if (playerInfo == null)
                {
                    playerInfo = PlayerInfo()
                    playerInfo!!.playerId = hazel.getTag().toByte()
//                    println("player id: ${playerInfo!!.playerId}")
                    playerInfo!!.deserialize(hazel.payload!!)
                    this.players[hazel.getTag().toByte()] = playerInfo!!
                    println("players: ${players.size}")
                } else {
                    playerInfo!!.deserialize(hazel.payload!!)
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
}