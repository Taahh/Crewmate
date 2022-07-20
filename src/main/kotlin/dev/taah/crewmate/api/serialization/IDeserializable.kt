package dev.taah.crewmate.api.serialization

import dev.taah.crewmate.util.PacketBuffer

interface IDeserializable<T> {
    fun deserialize(buffer: PacketBuffer): T
}