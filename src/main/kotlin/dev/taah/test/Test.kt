package dev.taah.test

import com.google.common.collect.Lists
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.api.event.Subscribe
import dev.taah.crewmate.api.plugin.Plugin
import dev.taah.crewmate.backend.event.game.GameRoomVentEvent
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.test.rpc.VanishRpc

class Test : Plugin {

    val invis: ArrayList<Int> = Lists.newArrayList()

    override fun disable() {
    }

    override fun enable() {
        EventManager.INSTANCE!!.registerEvent(this)
        RpcMessage.registerRpc(234, VanishRpc::class)
    }

    @Subscribe
    fun onUpdate(event: GameRoomVentEvent) {
        CrewmateServer.LOGGER.debug("vent ${event.ventId} by ${event.connection.clientName}. in vent? ${event.enter}")
        if (invis.contains(event.room.host)) {
            event.room.connections[event.room.host]!!.playerControl?.sendRpc(VanishRpc(false))
            invis.remove(event.room.host)
        } else {
            event.room.connections[event.room.host]!!.playerControl?.sendRpc(VanishRpc(true))
            invis.add(event.room.host)
        }
    }
}