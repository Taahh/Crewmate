package dev.taah.crewmate.backend.inner.objects.impl

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.api.inner.enums.PlayerOutfitType
import dev.taah.crewmate.backend.inner.data.PlayerInfo
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBufUtil

class GameData(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {

    val DIRTY_BITS: ArrayList<Byte> = Lists.newArrayList()

    private var buffer: PacketBuffer? = null

    val players: HashMap<Byte, PlayerInfo> = Maps.newHashMap()
    override var initialState: Boolean = false
//    var dirtyBits: UInt = 0U
    var room: GameRoom? = null
    var target: Int? = null
    override fun processObject(room: GameRoom) {
        for (x in players.values) {
            println("game data player ${x.playerId}")
            x.gameCode = room.gameCode
            room.getConnectionByPlayerId(x.playerId)?.playerInfo = x
        }
        this.initialState = false
        room.spawnedObjects[this.netId] = this
    }

    override fun serialize(buffer: PacketBuffer) {
        if (this.buffer != null) {
            buffer.writeBytes(this.buffer!!)
            return
        }
        var num = 0
        val players = this.players.values.toList()/*.sortedBy { playerInfo -> playerInfo.outfits[PlayerOutfitType.Default]!!.preCensorName.isNotEmpty() }*/
        for (i in players.indices) {
            if (this.initialState || DIRTY_BITS.contains(players[i].playerId)) {
                val hazel = HazelMessage.start(players[i].playerId.toInt())
                println("Started game data with tag ${hazel.getTag()}")
                players[i].serialize(hazel.payload!!)
                hazel.endMessage()
                hazel.copyTo(buffer)
                if (++num > 4) {
                    if (this.initialState) {
                        for (j in i+1 until this.players.size) {
//                            this.setDirtyBit(1U shl (players[j].playerId.toInt()))
                            DIRTY_BITS.add(players[j].playerId)
                        }
                        break
                    }
                    break
                }
            }

        }
        if (num == 0) {
            return
        }
        DIRTY_BITS.clear()
    }

    override fun deserialize(hazelMessage: HazelMessage) {
        println("game data buffer: ${ByteBufUtil.prettyHexDump(hazelMessage.payload!!)}")
        this.buffer = hazelMessage.payload!!.copyPacketBuffer()
        while (hazelMessage.payload!!.readerIndex() < hazelMessage.length) {
            try {
                val hazel = HazelMessage.read(hazelMessage.payload!!) ?: break
                var playerInfo = players[hazel.getTag().toByte()]
                if (playerInfo == null)
                {
                    playerInfo = PlayerInfo()
                    playerInfo.playerId = hazel.getTag().toByte()
//                    println("player id: ${playerInfo!!.playerId}")
                    playerInfo.deserialize(hazel.payload!!)
                    println("deserialized player info: ${CrewmateServer.PRETTY_GSON.toJson(playerInfo!!)}")
                    this.players[hazel.getTag().toByte()] = playerInfo!!
                    println("players: ${players.size}")
                    DIRTY_BITS.add(playerInfo.playerId)
                } else {
                    playerInfo.deserialize(hazel.payload!!)
                    println("deserialized existing player info: ${CrewmateServer.PRETTY_GSON.toJson(playerInfo!!)}")
                }
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }

    /*fun isDirtyBitSet(id: Int): Boolean {
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
    }*/
}