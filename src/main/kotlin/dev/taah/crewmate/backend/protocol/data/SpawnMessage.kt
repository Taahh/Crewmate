package dev.taah.crewmate.backend.protocol.data

import com.google.common.collect.Lists
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.InnerNetObjects
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.full.primaryConstructor

class SpawnMessage(val room: GameRoom) : AbstractMessage(0x04) {

    var innerNetObjects: ArrayList<InnerNetObjectWrapper> = Lists.newArrayList()

    fun addInnerNetObject(innerNetObjects: InnerNetObjects, ownerId: Int = -2): SpawnMessage {
        this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, null, innerNetObjects))
        return this
    }

    override fun processObject(room: GameRoom) {
        this.innerNetObjects.forEach {
            it.innerNetObject!!.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomSpawnEvent(room, it.innerNetObjects, it.innerNetObject))
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        this.innerNetObjects.forEach {
            val hazel = HazelMessage.start(0x04)
            hazel.payload!!.writePackedUInt32(it.innerNetObjects.spawnId.toLong())
            hazel.payload!!.writePackedInt32(it.ownerId)
            hazel.payload!!.writeByte(if (it.innerNetObjects == InnerNetObjects.PlayerControl) 1 else 0)
            hazel.payload!!.writePackedInt32(it.innerNetObjects.objects.size)
            if (it.innerNetObjects == InnerNetObjects.PlayerControl) {
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
                for (x in it.innerNetObjects.objects) {
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
                    innerNetObject.deserialize(hazel.payload!!)
                }
                this.innerNetObjects.add(InnerNetObjectWrapper(ownerId, innerNetObject, innerNetObjects))

            }
        }
    }

    class InnerNetObjectWrapper(val ownerId: Int = -2, val innerNetObject: AbstractInnerNetObject?, val innerNetObjects: InnerNetObjects) {
    }

}