package dev.taah.crewmate.core.test

import dev.taah.crewmate.api.event.Subscribe
import dev.taah.crewmate.backend.event.connection.GameRoomVisibilityUpdateEvent

class TestEvent {

    @Subscribe
    fun onGameJoin(event: GameRoomVisibilityUpdateEvent) {
        println("old ${event.old.name}; new ${event.new.name}")
    }

}