package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.inner.enums.PlayerOutfitType
import dev.taah.crewmate.api.inner.enums.RoleType
import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable
import dev.taah.crewmate.backend.inner.data.PlayerOutfit
import dev.taah.crewmate.backend.inner.data.TaskInfo

interface IPlayerInfo : ISerializable, IDeserializable<Unit> {
    val outfits: HashMap<PlayerOutfitType, PlayerOutfit>
    var playerId: Byte
    var tasks: ArrayList<TaskInfo>
    var level: Int
    var disconnected: Boolean
    var dead: Boolean
    var roleType: RoleType
    var friendCode: String
    var puid: String
}