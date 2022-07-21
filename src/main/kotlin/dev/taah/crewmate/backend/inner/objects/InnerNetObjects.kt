package dev.taah.crewmate.backend.inner.objects

import dev.taah.crewmate.api.inner.IInnerNetObject
import dev.taah.crewmate.backend.inner.objects.impl.*
import kotlin.reflect.KClass

enum class InnerNetObjects(val spawnId: Int, vararg objects: KClass<out AbstractInnerNetObject>) {
    GAME_DATA(3, GameData::class, VoteBanSystem::class),
    PLAYER_CONTROL(4, PlayerControl::class, PlayerPhysics::class, CustomNetworkTransform::class);

    val objects: ArrayList<KClass<out AbstractInnerNetObject>>

    init {
        this.objects = objects.toList() as ArrayList<KClass<out AbstractInnerNetObject>>
    }

    companion object {
        fun getBySpawnId(spawnId: Int): InnerNetObjects? {
            return values().find { it.spawnId == spawnId }
        }
    }
}