package dev.taah.crewmate.api.serialization

import dev.taah.crewmate.util.PacketBuffer

interface ISerializable {
    fun serialize(buffer: PacketBuffer)
}