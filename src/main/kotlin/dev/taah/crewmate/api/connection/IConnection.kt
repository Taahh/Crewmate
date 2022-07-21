package dev.taah.crewmate.api.connection

import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import dev.taah.crewmate.api.inner.enums.QuickChatMode
import dev.taah.crewmate.backend.inner.data.PlatformData
import dev.taah.crewmate.backend.inner.data.PlayerInfo
import dev.taah.crewmate.backend.inner.objects.impl.PlayerControl
import dev.taah.crewmate.util.inner.GameCode
import java.util.*

interface IConnection<P> {

    val uniqueId: UUID
    var clientName: String
    var clientVersion: Int
    var chatModeType: QuickChatMode
    var platformData: PlatformData
    var gameCode: GameCode?
    var playerInfo: PlayerInfo?

    fun sendPacket(packet: P, nonce: Int = -1)

    fun sendReliablePacket(packet: P)

    fun sendDisconnect(disconnectReasons: DisconnectReasons = DisconnectReasons.NotAuthorized, reason: String? = null)
}