package dev.taah.crewmate.backend.protocol.data

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomDataEvent
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.InnerNetObjects
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.full.primaryConstructor

class DataMessage : AbstractMessage(0x01) {

    var buffer: PacketBuffer? = null
    var netId: Int = 0

    override fun processObject(room: GameRoom) {
        if (room.spawnedObjects.containsKey(netId)) {
            CrewmateServer.LOGGER.debug("Updating ${room.spawnedObjects[netId]!!.javaClass.simpleName}")
            val obj = room.spawnedObjects[netId]!!
            obj.deserialize(buffer!!)
            obj.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, obj))
        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId }
            conn!!.playerControl!!.deserialize(buffer!!)
            conn.playerControl!!.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!))

        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId } != null) {
            val conn = room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId }
            conn!!.playerControl!!.playerPhysics!!.deserialize(buffer!!)
            conn.playerControl!!.playerPhysics!!.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!.playerPhysics!!))
        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId } != null) {
            val conn = room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId }
            conn!!.playerControl!!.customNetworkTransform!!.deserialize(buffer!!)
            conn.playerControl!!.customNetworkTransform!!.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!.customNetworkTransform!!))
        }
    }

    override fun serialize(buffer: PacketBuffer) {

    }

    override fun deserialize(buffer: PacketBuffer) {
        this.netId = buffer.readPackedUInt32().toInt()
        this.buffer = buffer
    }
}