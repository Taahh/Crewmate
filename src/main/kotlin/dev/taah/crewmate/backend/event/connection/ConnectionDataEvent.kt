package dev.taah.crewmate.backend.event.connection

import dev.taah.crewmate.api.connection.IConnection
import dev.taah.crewmate.api.event.IEvent
import dev.taah.crewmate.backend.connection.PlayerConnection

class ConnectionDataEvent(val connection: PlayerConnection) : IEvent {
}