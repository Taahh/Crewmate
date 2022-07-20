package dev.taah.crewmate.backend.protocol

import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable
import dev.taah.crewmate.backend.connection.PlayerConnection

abstract class AbstractPacket<in T: AbstractPacket<T>>(val packetType: Byte, var nonce: Int) : ISerializable,
    IDeserializable<Unit> {

    abstract fun processPacket(packet: T, connection: PlayerConnection);
}