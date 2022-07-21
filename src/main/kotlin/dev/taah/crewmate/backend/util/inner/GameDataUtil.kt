package dev.taah.crewmate.backend.util.inner

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomDataEvent
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.inner.objects.InnerNetObjects
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.full.primaryConstructor

class GameDataUtil {
    companion object {
        fun handleGameData(buffer: PacketBuffer, room: GameRoom) {
            var hazel = HazelMessage.read(buffer)
            while (hazel != null) {
//                println("tag: ${hazel.getTag()}")
                when (hazel.getTag()) {
                    4 -> {
                        println("Game Data Spawn Message")
                        handleSpawn(hazel.payload!!, room)
                    }
                    1 -> {
                        val netId = hazel.payload!!.readPackedUInt32().toInt()
                        if (room.spawnedObjects.containsKey(netId)) {
                            CrewmateServer.LOGGER.debug("Updating ${room.spawnedObjects[netId]!!.javaClass.simpleName}")
                            val obj = room.spawnedObjects[netId]!!
                            obj.deserialize(hazel.payload!!)
                            obj.processObject(room)
                            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, obj))
                        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId } != null) {
                            val conn =
                                room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId }
                            conn!!.playerControl!!.deserialize(hazel.payload!!)
                            conn.playerControl!!.processObject(room)
                            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!))

                        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId } != null) {
                            val conn = room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId }
                            conn!!.playerControl!!.playerPhysics!!.deserialize(hazel.payload!!)
                            conn.playerControl!!.playerPhysics!!.processObject(room)
                            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!.playerPhysics!!))
                        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId } != null) {
                            val conn = room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId }
                            conn!!.playerControl!!.customNetworkTransform!!.deserialize(hazel.payload!!)
                            conn.playerControl!!.customNetworkTransform!!.processObject(room)
                            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!.customNetworkTransform!!))
                        }
                    }
                    else -> {
                        CrewmateServer.LOGGER.warn("Unknown Game Data Flag: ${hazel.getTag()}")
                    }
                }
                hazel = HazelMessage.read(buffer)
            }
        }

        private fun handleSpawn(buffer: PacketBuffer, room: GameRoom) {
            val spawnId = buffer.readPackedUInt32()
            val ownerId = buffer.readPackedInt32()
            val flags = buffer.readByte()
            val components = buffer.readPackedInt32()

            val innerNetObjects = InnerNetObjects.getBySpawnId(spawnId.toInt())
            if (innerNetObjects != null) {
                CrewmateServer.LOGGER.debug("Inner Net Object Parent: ${innerNetObjects.name}")
                for (i in 0 until components) {
                    val netId = buffer.readPackedUInt32()
                    val hazel = HazelMessage.read(buffer)!!
                    println("Tag ${hazel.getTag()} net id $netId and length ${hazel.length}")
                    if (i >= innerNetObjects.objects.size) {
                        continue
                    }
                    val obj = innerNetObjects.objects[i]
                    val innerNetObject = obj.primaryConstructor!!.call(netId.toInt(), ownerId)
                    innerNetObject.initialState = true
                    CrewmateServer.LOGGER.debug("Spawning ${innerNetObject.javaClass.simpleName} with owner ID $ownerId")
                    if (hazel.length > 0) {
//                        println("buffer: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")
                        innerNetObject.deserialize(hazel.payload!!)
                    }
                    innerNetObject.processObject(room)
                    EventManager.INSTANCE!!.callEvent(GameRoomSpawnEvent(room, innerNetObjects, innerNetObject))
                }
            }
        }
    }
}