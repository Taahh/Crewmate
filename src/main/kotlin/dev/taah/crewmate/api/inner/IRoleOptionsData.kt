package dev.taah.crewmate.api.inner

import dev.taah.crewmate.api.inner.enums.RoleType
import dev.taah.crewmate.api.inner.enums.TaskBarMode
import dev.taah.crewmate.api.serialization.IDeserializable
import dev.taah.crewmate.api.serialization.ISerializable

/**
 * @author Taah
 * @project crewmate
 * @since 7:03 PM [20-05-2022]
 */
interface IRoleOptionsData<R> : ISerializable, IDeserializable<IRoleOptionsData<R>> {
    val roleRates: HashMap<RoleType, R>
    var shapeshifterLeaveSkin: Boolean
    var shapeshifterCooldown: Byte
    var shapeshifterDuration: Byte
    var scientistCooldown: Byte
    var guardianAngelCooldown: Byte
    var engineerCooldown: Byte
    var engineerVentMaxTime: Byte
    var scientistBatteryCharge: Byte
    var protectionDuration: Byte
    var impostersSeeProtected: Boolean
}