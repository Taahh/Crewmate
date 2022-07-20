package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable

interface IPlatformData : ISerializable, IDeserializable<IPlatformData> {
    var platform: Byte
    var platformName: String
}