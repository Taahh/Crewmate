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
    var dirtyBits: UInt = 0U
    var room: GameRoom? = null
    var target: Int? = null
    override fun processObject(room: GameRoom) {
        for (x in players.values) {
            println("game data player ${x.playerId}")
            room.getConnectionByPlayerId(x.playerId)?.playerInfo = x
        }
        this.initialState = false
        room.spawnedObjects[this.netId] = this
    }

    override fun serialize(buffer: PacketBuffer) {
        var num = 0
        val players = this.players.values.toList()
        for (i in 0 until players.size) {
            if (room != null && target != null && target!! == room!!.connections.entries.first { it.value.uniqueId.equals(room!!.getConnectionByPlayerId(players[i].playerId)!!.uniqueId) }.key) {
                continue
            }
            println("dirty bit for ${players[i].playerId}? ${this.isDirtyBitSet(players[i].playerId.toInt())}")
            /*if (this.initialState || this.isDirtyBitSet(players[i].playerId.toInt())) {
                if (!this.initialState) {
                    this.unsetDirtyBit(players[i].playerId.toUInt())
                }
                val hazel = HazelMessage.start(players[i].playerId.toInt())
                println("Started game data with tag ${hazel.getTag()}")
                players[i].serialize(hazel.payload!!)
                hazel.endMessage()
                hazel.copyTo(buffer)
                if (++num > 4) {
                    if (this.initialState) {
                        for (j in i+1 until this.players.size) {
                            this.setDirtyBit(1U shl (players[j].playerId.toInt()))
                        }
                    }
                }
            }*/
            val hazel = HazelMessage.start(players[i].playerId.toInt())
            println("Started game data with tag ${hazel.getTag()}")
            players[i].serialize(hazel.payload!!)
            hazel.endMessage()
            hazel.copyTo(buffer)
            if (++num > 4) {
                if (this.initialState) {
                    for (j in i+1 until this.players.size) {
                        this.setDirtyBit(1U shl (players[j].playerId.toInt()))
                    }
                }
            }
        }
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

    fun isDirtyBitSet(id: Int): Boolean {
        val check = (this.dirtyBits and ((1 shl id).toUInt()))
        return  check > 0U
    }

    fun clearDirtyBits() {
        this.dirtyBits = 0U
    }

    fun unsetDirtyBit(value: UInt) {
        this.dirtyBits = this.dirtyBits and value.inv()
    }
    fun setDirtyBit(value: UInt) {
        this.dirtyBits = this.dirtyBits or value
    }
}