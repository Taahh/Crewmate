package dev.taah.crewmate.api.connection

import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import java.util.*

interface IConnection<P> {

    val uniqueId: UUID
    var clientName: String

    fun sendPacket(packet: P, nonce: Int = -1)

    fun sendReliablePacket(packet: P)

    fun sendDisconnect(disconnectReasons: DisconnectReasons = DisconnectReasons.NotAuthorized, reason: String? = null)
}