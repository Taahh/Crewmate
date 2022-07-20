package dev.taah.crewmate.api.room

import dev.taah.crewmate.api.inner.enums.GameState
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.util.inner.GameCode

interface IRoom<P> {
    var gameCode: GameCode
    var players: HashMap<Int, PlayerConnection>
    var waitingForHost: ArrayList<Int>
    var host: Int
    var state: GameState

    fun broadcastPacket(packet: P, reliable: Boolean = false, vararg exclude: Int)
}