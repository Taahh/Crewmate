package dev.taah.crewmate.core.room

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.api.connection.IConnection
import dev.taah.crewmate.api.inner.IInnerNetObject
import dev.taah.crewmate.api.inner.enums.GameState
import dev.taah.crewmate.api.inner.enums.GameVisibility
import dev.taah.crewmate.api.room.IRoom
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.data.PlayerInfo
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.util.inner.GameCode

class GameRoom(override var gameCode: GameCode) : IRoom<AbstractPacket<*>, PlayerConnection, PlayerInfo> {
    companion object {
        val ROOMS: HashMap<GameCode, GameRoom> = Maps.newHashMap()
        fun exists(code: GameCode): Boolean {
            return ROOMS.keys.any { it.equals(code) }
        }

        fun get(code: GameCode): GameRoom {
            return ROOMS[ROOMS.filter { entry -> entry.key.equals(code) }.keys.first()]!!
        }
    }

    override var connections: HashMap<Int, PlayerConnection> = Maps.newHashMap()
    override val spawnedObjects: HashMap<Int, AbstractInnerNetObject> = Maps.newHashMap()
    override var waitingForHost: ArrayList<Int> = Lists.newArrayList()
    override var host: Int = -1
    override var state: GameState = GameState.NotStarted
    override var visibility: GameVisibility = GameVisibility.Private
    override var gameOptionsData: GameOptionsData? = null
    var gameData: GameData? = null
        get() {
            return this.spawnedObjects.values.find { inner -> inner.isGameData() } as GameData?
        }
        private set

    override fun getConnectionByClientId(id: Int): PlayerConnection? {
        return connections[id]
    }

    override fun getConnectionByPlayerId(id: Byte): PlayerConnection? {
        return connections.values.find { it.playerControl != null && it.playerControl!!.playerId == id }
    }

    fun broadcastReliablePacket(packet: AbstractPacket<*>, vararg exclude: Int) {
        broadcastPacket(packet, true, *exclude)
    }

    override fun broadcastPacket(packet: AbstractPacket<*>, reliable: Boolean, vararg exclude: Int) {
        println("Broadcasting packet ${if (packet is ReliablePacket) packet.reliablePacket!!.javaClass.simpleName else packet.javaClass.simpleName}")
        if (exclude.isNotEmpty()) {
            val connections = this.connections.keys.toMutableSet()
            for (x in exclude) {
                connections.remove(x)
            }
            for (x in connections) {
                if (reliable) {
                    this.connections[x]!!.sendReliablePacket(packet)
                } else {
                    this.connections[x]!!.sendPacket(packet)
                }
            }
        } else {
            for (x in this.connections.keys) {
                if (reliable) {
                    this.connections[x]!!.sendReliablePacket(packet)
                } else {
                    this.connections[x]!!.sendPacket(packet)
                }
            }
        }
        println("---------------------------------------------------------")
    }

    fun update() {
        ROOMS[get(this.gameCode).gameCode] = this
    }
}