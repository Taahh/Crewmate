package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable

interface ITaskInfo : ISerializable, IDeserializable<Unit> {
    var id: Int
    var typeId: Byte
    var complete: Boolean
}