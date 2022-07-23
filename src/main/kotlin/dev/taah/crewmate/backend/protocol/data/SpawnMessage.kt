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
        when (spawnId) {
            2 -> {
                val lobbyBehavior = room.spawnedObjects.values.filter { entry -> entry.isLobbyBehavior() }
                    innerNetObjects.add(InnerNetObjectWrapper(ownerId, lobbyBehavior, spawnId))
            }
            3 -> {
                val gameData = room.spawnedObjects.values.filter { entry -> entry.isGameData() }.toMutableList()
                val voteBanSystem = room.spawnedObjects.values.filter { entry -> entry.isVoteBanSystem() }
                gameData.addAll(voteBanSystem)
                innerNetObjects.add(InnerNetObjectWrapper(ownerId, gameData, spawnId))
            }
            4 -> {
                innerNetObjects.add(InnerNetObjectWrapper(ownerId, room.connections[ownerId]!!.getPlayerControlObjects().toList(), spawnId))
            }
        }
        return this
    }

    override fun processObject(room: GameRoom) {
        this.innerNetObjects.forEach {
            it.innerNetObjects!!.forEach { obj -> obj.processObject(room) }
            EventManager.INSTANCE!!.callEvent(GameRoomSpawnEvent(room, it.spawnId, it.innerNetObjects))
        }

        if (this.target != null) {
            CrewmateServer.LOGGER.debug("TARGET WAS NOT NULL, SENDING GAME DATA TO FOR SPAWN")
            /*val spawnMessage = SpawnMessage(room).addInnerNetObject(3).addInnerNetObject(2)

            for ((k, v) in room.connections) {
                spawnMessage.addInnerNetObject(4, k)
            }
            spawnMessage.sender = sender
            spawnMessage.target = target*/
            room.connections[this.target]!!.sendReliablePacket(GameDataToPacket().target(this.target!!).gameCode(room.gameCode).addMessage(this)/*.addMessage(DataMessage(room.spawnedObjects.values.first { entry -> entry.isGameData() }))*/)
        } else {
            CrewmateServer.LOGGER.debug("SENDING GAME DATA PAKCET FOR SPAWN")
            if (sender != null) {
                CrewmateServer.LOGGER.debug("SENDER WAS NOT NULL")
                val entry = room.connections.entries.find { entry -> entry.value.uniqueId.equals(this.sender!!.uniqueId) }
                /*val spawnMessage = SpawnMessage(room).addInnerNetObject(3).addInnerNetObject(2)

                for ((k, v) in room.connections) {
                    spawnMessage.addInnerNetObject(4, k)
                }
                spawnMessage.sender = sender
                spawnMessage.target = target*/
                room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(this), entry!!.key)
            } else {
                CrewmateServer.LOGGER.debug("SENDER WAS NULL")
                /*val spawnMessage = SpawnMessage(room).addInnerNetObject(3).addInnerNetObject(2)

                for ((k, v) in room.connections) {
                    spawnMessage.addInnerNetObject(4, k)
                }
                spawnMessage.sender = sender
                spawnMessage.target = target*/
                room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(this))

            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        if (this.buffer != null) {
            val hazel = HazelMessage.start(0x04)
            hazel.payload!!.writeBytes(this.buffer!!)
            hazel.endMessage()
            hazel.copyTo(buffer)
        } else {
            this.innerNetObjects.forEach {
                val hazel = HazelMessage.start(0x04)
                hazel.payload!!.writePackedUInt32(it.spawnId.toLong())
                hazel.payload!!.writePackedInt32(it.ownerId)
                hazel.payload!!.writeByte(if (it.spawnId == 4) 1 else 0)

                val innerNetObjects = GameDataUtil.getInner(it.spawnId) ?: throw RuntimeException("Inner Net Object with spawn Id ${it.spawnId} was not registered!")

                hazel.payload!!.writePackedInt32(innerNetObjects!!.size)
                for (obj in it.innerNetObjects!!) {
                    hazel.payload!!.writePackedUInt32(obj.netId.toLong())
                    val hazelInner = HazelMessage.start(0x01)
                    obj.initialState = true
                    CrewmateServer.LOGGER.debug("serializing ${obj.javaClass.simpleName} with owner id ${obj.ownerId} and net id ${obj.netId} and spawn id ${it.spawnId}")
                    obj.serialize(hazelInner.payload!!)
                    obj.initialState = false
                    hazelInner.endMessage()
                    hazelInner.copyTo(hazel.payload!!)
                }

                hazel.endMessage()
                hazel.copyTo(buffer)
            }
        }

    }

    override fun deserialize(buffer: PacketBuffer) {
//        this.buffer = buffer.copyPacketBuffer()
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
                    innerNetObject.deserialize(hazel)
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