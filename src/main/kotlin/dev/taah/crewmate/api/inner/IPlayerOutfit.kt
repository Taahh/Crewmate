package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable

interface IPlayerOutfit : ISerializable, IDeserializable<Unit> {
    var colorId: Int
    var hatId: String
    var petId: String
    var skinId: String
    var visorId: String
    var namePlateId: String
    var preCensorName: String
}