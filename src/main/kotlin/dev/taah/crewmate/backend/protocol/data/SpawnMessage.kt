package dev.taah.crewmate.backend.protocol.data

import com.google.common.collect.Lists
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.util.inner.GameDataUtil
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.full.primaryConstructor

class SpawnMessage(val room: GameRoom) : AbstractMessage(0x04) {

    var innerNetObjects: ArrayList<InnerNetObjectWrapper> = Lists.newArrayList()

    fun addInnerNetObject(spawnId: Int, ownerId: Int = -2): SpawnMessage {
        this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, null, spawnId))
        return this
    }

    override fun processObject(room: GameRoom) {
        this.innerNetObjects.forEach {
            it.innerNetObject!!.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomSpawnEvent(room, it.spawnId, it.innerNetObject))
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
            if (it.spawnId == 4) {
                val playerControl = room.getConnectionByClientId(it.ownerId)
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
            } else {
                for (x in innerNetObjects) {
                    val obj = room.spawnedObjects.values.find { x.simpleName == it.javaClass.simpleName }
                    if (obj == null) {
                        CrewmateServer.LOGGER.error("Couldn't find spawned object of {${x.simpleName}")
                        continue
                    }
                    CrewmateServer.LOGGER.debug("Serializing ${x.simpleName} in spawn message!")
                    hazel.payload!!.writePackedUInt32(obj.netId.toLong())
                    val innerHazel = HazelMessage.start(0x01)
                    obj.initialState = true
                    obj.serialize(innerHazel.payload!!)
                    obj.initialState = false
                    innerHazel.endMessage()
                    innerHazel.copyTo(hazel.payload!!)
                }
            }

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
                this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, innerNetObject, spawnId.toInt()))

            }
        }
    }

    class InnerNetObjectWrapper(val ownerId: Int = -2, val innerNetObject: AbstractInnerNetObject?, val spawnId: Int) {
    }

}