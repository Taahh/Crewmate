package dev.taah.crewmate.api.room

import dev.taah.crewmate.api.connection.IConnection
import dev.taah.crewmate.api.inner.enums.GameState
import dev.taah.crewmate.api.inner.enums.GameVisibility
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.api.inner.IInnerNetObject
import dev.taah.crewmate.api.inner.IPlayerInfo
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.util.inner.GameCode

interface IRoom<P, T: IConnection<*>, C: IPlayerInfo> {
    var gameCode: GameCode
    var connections: HashMap<Int, T>
    val spawnedObjects: HashMap<Int, out IInnerNetObject<*>>
    var waitingForHost: ArrayList<Int>
    var host: Int
    var state: GameState
    var visibility: GameVisibility
    var gameOptionsData: GameOptionsData?

    fun broadcastPacket(packet: P, reliable: Boolean = false, vararg exclude: Int)

    fun getConnectionByClientId(id: Int): T?
    fun getConnectionByPlayerId(id: Byte): T?
}