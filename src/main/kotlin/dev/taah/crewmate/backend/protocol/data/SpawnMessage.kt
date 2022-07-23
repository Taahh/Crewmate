package dev.taah.crewmate.backend.protocol.data

import com.google.common.collect.Lists
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.backend.protocol.root.GameDataToPacket
import dev.taah.crewmate.backend.util.inner.GameDataUtil
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import java.util.function.Consumer
import kotlin.reflect.full.primaryConstructor

class SpawnMessage(val room: GameRoom) : AbstractMessage() {

    var innerNetObjects: ArrayList<InnerNetObjectWrapper> = Lists.newArrayList()

    var buffer: PacketBuffer? = null

    fun addInnerNetObject(spawnId: Int, ownerId: Int = -2): SpawnMessage {
        this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, null, spawnId))
        return this
    }

    override fun processObject(room: GameRoom) {
        this.innerNetObjects.forEach {
            it.innerNetObjects!!.forEach { obj -> obj.processObject(room) }
            EventManager.INSTANCE!!.callEvent(GameRoomSpawnEvent(room, it.spawnId, it.innerNetObjects))
        }

        if (this.target != null) {
            CrewmateServer.LOGGER.debug("TARGET WAS NOT NULL, SENDING GAME DATA TO FOR SPAWN")
            val spawnMessage = SpawnMessage(room).addInnerNetObject(3).addInnerNetObject(2)

            for ((k, v) in room.connections) {
                if (k == target) {
                    continue
                }
                spawnMessage.addInnerNetObject(4, k)
            }
            spawnMessage.sender = sender
            spawnMessage.target = target
            room.connections[this.target]?.sendReliablePacket(GameDataToPacket().target(this.target!!).gameCode(room.gameCode).addMessage(this))
        } else {
            CrewmateServer.LOGGER.debug("SENDING GAME DATA PAKCET FOR SPAWN")
            if (sender != null) {
                CrewmateServer.LOGGER.debug("SENDER WAS NOT NULL")
                val entry = room.connections.entries.find { entry -> entry.value.uniqueId.equals(this.sender!!.uniqueId) }
                val spawnMessage = SpawnMessage(room).addInnerNetObject(3).addInnerNetObject(2)

                for ((k, v) in room.connections) {
                    if (v.uniqueId.equals(sender!!.uniqueId)) {
                        continue
                    }
                    spawnMessage.addInnerNetObject(4, k)
                }
                spawnMessage.sender = sender
                spawnMessage.target = target
                room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(this), entry!!.key)
            } else {
                CrewmateServer.LOGGER.debug("SENDER WAS NULL")
                val spawnMessage = SpawnMessage(room).addInnerNetObject(3).addInnerNetObject(2)

                for ((k, v) in room.connections) {
                    spawnMessage.addInnerNetObject(4, k)
                }
                spawnMessage.sender = sender
                spawnMessage.target = target
                room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(this))
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        this.innerNetObjects.forEach {
            val hazel = HazelMessage.start(0x04)
            hazel.payload!!.writePackedUInt32(it.spawnId.toLong())
            hazel.payload!!.writePackedInt32(it.ownerId)
            hazel.payload!!.writeByte(if (it.spawnId == 4) 1 else 0)

            val innerNetObjects = GameDataUtil.getInner(it.spawnId) ?: throw RuntimeException("Inner Net Object with spawn Id ${it.spawnId} was not registered!")

            hazel.payload!!.writePackedInt32(innerNetObjects!!.size)
            it.innerNetObjects!!.forEach(Consumer { obj ->
                run {
                    val hazelInner = HazelMessage.start(0x01)
                    obj.initialState = true
                    CrewmateServer.LOGGER.debug("serializing ${obj.javaClass.simpleName} with owner id ${obj.ownerId} and net id ${obj.netId} and spawn id ${it.spawnId}")
                    obj.serialize(hazelInner.payload!!)
                    obj.initialState = false
                    hazelInner.endMessage()
                    hazelInner.copyTo(hazel.payload!!)
                }
            })
            /*if (it.spawnId == 4) {
                CrewmateServer.LOGGER.debug("SERIALIZING OWNER ID'S PLAYER STUFF ${it.ownerId}")
                val playerControl = room.getConnectionByClientId(it.ownerId)
                if (playerControl?.getPlayerControlObjects()!!.size >= 3) {
                    for (x in playerControl!!.getPlayerControlObjects()) {
                        CrewmateServer.LOGGER.debug("Serializing ${x.javaClass.simpleName} in spawn message!")
                        hazel.payload!!.writePackedUInt32(x.netId.toLong())
                        val innerHazel = HazelMessage.start(0x01)
                        x.initialState = true
                        x.serialize(innerHazel.payload!!)
                        x.initialState = false
                        innerHazel.endMessage()
                        innerHazel.copyTo(hazel.payload!!)
                    }
                }
            } else {
                for (x in innerNetObjects) {
                    val obj = room.spawnedObjects.values.find { x.simpleName == it.javaClass.simpleName }
                    if (obj == null) {
                        CrewmateServer.LOGGER.error("Couldn't find spawned object of {${x.simpleName}")
                        continue
                    }
                        *//*if (obj.isGameData()) {
                            println("sender ${sender!!.clientName}")
                            (obj as GameData).target = sender!!.playerControl!!.playerId.toInt()
                            obj.room = room
                        }*//*
                    CrewmateServer.LOGGER.debug("Serializing ${x.simpleName} in spawn message!")
                    hazel.payload!!.writePackedUInt32(obj.netId.toLong())
                    val innerHazel = HazelMessage.start(0x01)
                    obj.initialState = true
                    obj.serialize(innerHazel.payload!!)
                    obj.initialState = false
                    innerHazel.endMessage()
                    innerHazel.copyTo(hazel.payload!!)
                }
            }*/

            hazel.endMessage()
            hazel.copyTo(buffer)
        }
    }

    override fun deserialize(buffer: PacketBuffer) {
        val spawnId = buffer.readPackedUInt32()
        val ownerId = buffer.readPackedInt32()
        val flags = buffer.readByte()
        val components = buffer.readPackedInt32()


        val innerNetObjects = GameDataUtil.getInner(spawnId.toInt())

        val componentsList: ArrayList<AbstractInnerNetObject> = Lists.newArrayList()

        if (innerNetObjects != null) {
            for (i in 0 until components) {
                val netId = buffer.readPackedUInt32()
                val hazel = HazelMessage.read(buffer)!!
                println("Tag ${hazel.getTag()} net id $netId and length ${hazel.length}")
                if (i >= innerNetObjects.size) {
                    continue
                }
                val obj = innerNetObjects[i]
                val innerNetObject = obj.primaryConstructor!!.call(netId.toInt(), ownerId)
                innerNetObject.initialState = true
                CrewmateServer.LOGGER.debug("Spawning ${innerNetObject.javaClass.simpleName} with owner ID $ownerId")
                if (hazel.length > 0) {
                    innerNetObject.deserialize(hazel.payload!!)
                }
//                this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, innerNetObject, spawnId.toInt()))
                componentsList.add(innerNetObject)
            }
            this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, componentsList, spawnId.toInt()))

        }
    }

    class InnerNetObjectWrapper(val ownerId: Int = -2, val innerNetObjects: List<AbstractInnerNetObject>?, val spawnId: Int) {
    }

}