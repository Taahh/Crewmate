package dev.taah.crewmate.core.test

import dev.taah.crewmate.api.event.Subscribe
import dev.taah.crewmate.backend.event.room.GameRoomDataEvent
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.event.room.GameRoomVisibilityUpdateEvent
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.backend.inner.objects.impl.PlayerControl

class TestEvent {

    /*@Subscribe
    fun onGameJoin(event: GameRoomSpawnEvent) {
        if (event.innerNetObject.isPlayerControl()) {
            val control = event.innerNetObject as PlayerControl
            println("Added player control to ${event.room.getConnectionByPlayerId(control.playerId)?.clientName}")
        }
    }

    @Subscribe
    fun onGameJoin(event: GameRoomDataEvent) {
        if (event.innerNetObject.isGameData()) {
            val control = event.innerNetObject as GameData
            for (x in control.players) {
                println("user: ${event.room.getConnectionByPlayerId(x.key)}, playerinfo? ${event.room.getConnectionByPlayerId(x.key)!!.playerInfo != null}")
            }
        }
    }*/

}