package dev.taah.crewmate.backend.protocol.data

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomDataEvent
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.backend.protocol.root.GameDataToPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBufUtil

class DataMessage() : AbstractMessage() {

    private var innerNetObject: AbstractInnerNetObject? = null

    var actualBuffer: PacketBuffer? = null
    var buffer: HazelMessage? = null
    var netId: Int = 0


    constructor(innerNetObject: AbstractInnerNetObject) : this() {
        this.innerNetObject = innerNetObject
    }

    override fun processObject(room: GameRoom) {
        if (room.spawnedObjects.containsKey(netId)) {
            CrewmateServer.LOGGER.debug("Updating ${room.spawnedObjects[netId]!!.javaClass.simpleName}")
            this.innerNetObject = room.spawnedObjects[netId]!!
            this.innerNetObject!!.deserialize(buffer!!)
            this.innerNetObject!!.processObject(room)
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, this.innerNetObject!!))
        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId }
            conn!!.playerControl!!.deserialize(buffer!!)
            conn.playerControl!!.processObject(room)
            this.innerNetObject = conn.playerControl
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!))

        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId }
            conn!!.playerControl!!.playerPhysics!!.deserialize(buffer!!)
            conn.playerControl!!.playerPhysics!!.processObject(room)
            this.innerNetObject = conn.playerControl!!.playerPhysics
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!.playerPhysics!!))
        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId }
            conn!!.playerControl!!.customNetworkTransform!!.deserialize(buffer!!)
            conn.playerControl!!.customNetworkTransform!!.processObject(room)
            this.innerNetObject = conn.playerControl!!.customNetworkTransform
            EventManager.INSTANCE!!.callEvent(GameRoomDataEvent(room, conn.playerControl!!.customNetworkTransform!!))
        }
        if (this.innerNetObject != null) {
            if (this.target != null) {
                CrewmateServer.LOGGER.debug("TARGET WAS NOT NULL, SENDING GAME DATA TO FOR DATA")
                room.connections[this.target]?.sendReliablePacket(GameDataToPacket().target(this.target!!).gameCode(room.gameCode).addMessage(/*DataMessage(getByNetId(room, this.innerNetObject!!.netId)!!)*/this))
            } else {
                CrewmateServer.LOGGER.debug("SENDING GAME DATA PAKCET FOR DATA")
                if (sender != null) {
                    CrewmateServer.LOGGER.debug("SENDER WAS NOT NULL")
                    val entry =
                        room.connections.entries.find { entry -> entry.value.uniqueId.equals(this.sender!!.uniqueId) }
                    room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(/*DataMessage(getByNetId(room, this.innerNetObject!!.netId)!!)*/this), entry!!.key)
                } else {
                    CrewmateServer.LOGGER.debug("SENDER WAS NULL")
                    room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(/*DataMessage(getByNetId(room, this.innerNetObject!!.netId)!!)*/this))
                }
            }
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        println("serializing data")
        val hazel = HazelMessage.start(0x01)
        if (this.actualBuffer != null) {
            hazel.payload!!.writeBytes(this.actualBuffer!!)
        } else {
            hazel.payload!!.writePackedUInt32(this.innerNetObject!!.netId.toLong())
            println("serializing ${this.innerNetObject!!.javaClass.simpleName} for data")
            this.innerNetObject!!.initialState = false
            this.innerNetObject!!.serialize(hazel.payload!!)
        }
        hazel.endMessage()
        hazel.copyTo(buffer)
        println("actual buffer: ${ByteBufUtil.prettyHexDump(buffer)}")
    }

    override fun deserialize(buffer: PacketBuffer) {
//        this.actualBuffer = buffer.copyPacketBuffer()
        this.netId = buffer.readPackedUInt32().toInt()
        val hazel = HazelMessage()
        hazel.length = buffer.readableBytes()
        hazel.payload = buffer
        this.buffer = hazel

    }

    fun getByNetId(room: GameRoom, netId: Int): AbstractInnerNetObject? {
        if (room.spawnedObjects.containsKey(netId)) {
            return room.spawnedObjects[netId]!!
        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.netId == netId }
            return conn!!.playerControl!!

        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.playerPhysics != null && it.playerControl!!.playerPhysics!!.netId == netId }
            return conn!!.playerControl!!.playerPhysics!!
        } else if (room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId } != null) {
            val conn =
                room.connections.values.find { it.playerControl != null && it.playerControl!!.customNetworkTransform != null && it.playerControl!!.customNetworkTransform!!.netId == netId }
            return conn!!.playerControl!!.customNetworkTransform!!
        }
        return null
    }
}