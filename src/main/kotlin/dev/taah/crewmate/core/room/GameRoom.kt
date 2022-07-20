package dev.taah.crewmate.core.room

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.api.inner.enums.GameState
import dev.taah.crewmate.api.inner.enums.GameVisibility
import dev.taah.crewmate.api.room.IRoom
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.util.inner.GameCode

class GameRoom(override var gameCode: GameCode) : IRoom<AbstractPacket<*>> {
    companion object {
        val ROOMS: HashMap<GameCode, GameRoom> = Maps.newHashMap()
        fun exists(code: GameCode): Boolean {
            return ROOMS.keys.any { it.equals(code) }
        }

        fun get(code: GameCode): GameRoom {
            return ROOMS[ROOMS.filter { entry -> entry.key.equals(code) }.keys.first()]!!
        }
    }

    override var players: HashMap<Int, PlayerConnection> = Maps.newHashMap()
    override var waitingForHost: ArrayList<Int> = Lists.newArrayList()
    override var host: Int = -1
    override var state: GameState = GameState.NotStarted
    override var visibility: GameVisibility = GameVisibility.Private
    fun broadcastReliablePacket(packet: AbstractPacket<*>, vararg exclude: Int) {
        broadcastPacket(packet, true, *exclude)
    }

    override fun broadcastPacket(packet: AbstractPacket<*>, reliable: Boolean, vararg exclude: Int) {
        println("Broadcasting packet ${if (packet is ReliablePacket) packet.reliablePacket!!.javaClass.simpleName else packet.javaClass.simpleName}")
        if (exclude.isNotEmpty()) {
            val players = this.players.keys.toMutableSet()
            for (x in exclude) {
                players.remove(x)
            }
            for (x in players) {
                if (reliable) {
                    this.players[x]!!.sendReliablePacket(packet)
                } else {
                    this.players[x]!!.sendPacket(packet)
                }
            }
        } else {
            for (x in this.players.keys) {
                if (reliable) {
                    this.players[x]!!.sendReliablePacket(packet)
                } else {
                    this.players[x]!!.sendPacket(packet)
                }
            }
        }
        println("---------------------------------------------------------")
    }

    fun update() {
        ROOMS[get(this.gameCode).gameCode] = this
    }
}