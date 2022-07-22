package dev.taah.crewmate.backend.inner.objects

import dev.taah.crewmate.backend.inner.objects.impl.CustomNetworkTransform
import dev.taah.crewmate.backend.inner.objects.impl.PlayerPhysics
import dev.taah.crewmate.backend.inner.objects.impl.VoteBanSystem
import kotlin.reflect.KClass

enum class InnerNetObjects(val spawnId: Int, vararg objects: KClass<out AbstractInnerNetObject>) {
    LobbyBehavior(2, dev.taah.crewmate.backend.inner.objects.impl.LobbyBehavior::class),
    GameData(3, dev.taah.crewmate.backend.inner.objects.impl.GameData::class, VoteBanSystem::class),
    PlayerControl(4, dev.taah.crewmate.backend.inner.objects.impl.PlayerControl::class, PlayerPhysics::class, CustomNetworkTransform::class);

    val objects: List<KClass<out AbstractInnerNetObject>>

    init {
        this.objects = objects.toList()
    }

    companion object {
        fun getBySpawnId(spawnId: Int): InnerNetObjects? {
            return values().find { it.spawnId == spawnId }
        }
    }
}