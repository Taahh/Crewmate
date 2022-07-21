package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.data.PlayerInfo
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBufUtil

class GameData(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override fun processObject(room: GameRoom) {
        room.spawnedObjects[this.netId] = this
    }

    override fun serialize(buffer: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun deserialize(buffer: PacketBuffer, room: GameRoom) {
        while (buffer.readableBytes() > 0) {
            try {
                val hazel = HazelMessage.read(buffer) ?: break
                println("other buffer: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")
                var playerInfo = room.players[hazel.getTag().toByte()]
                if (playerInfo == null)
                {
                    playerInfo = PlayerInfo()
                    playerInfo!!.playerId = hazel.getTag().toByte()
                    println("player id: ${playerInfo!!.playerId}")
                    playerInfo!!.deserialize(hazel.payload!!)
                    room.players[hazel.getTag().toByte()] = playerInfo!!
                } else {
                    playerInfo!!.deserialize(hazel.payload!!)
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }
}