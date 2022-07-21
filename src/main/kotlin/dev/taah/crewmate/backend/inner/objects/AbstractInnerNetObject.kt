package dev.taah.crewmate.backend.inner.objects

import dev.taah.crewmate.api.inner.IInnerNetObject
import dev.taah.crewmate.backend.inner.objects.impl.*
import dev.taah.crewmate.core.room.GameRoom

abstract class AbstractInnerNetObject : IInnerNetObject<GameRoom> {

    fun isPlayerControl(): Boolean {
        return this is PlayerControl
    }
    fun isPlayerPhysics(): Boolean {
        return this is PlayerPhysics
    }
    fun isCustomNetworkTransform(): Boolean {
        return this is CustomNetworkTransform
    }
    fun isGameData(): Boolean {
        return this is GameData
    }
    fun isVoteBanSystem(): Boolean {
        return this is VoteBanSystem
    }
}