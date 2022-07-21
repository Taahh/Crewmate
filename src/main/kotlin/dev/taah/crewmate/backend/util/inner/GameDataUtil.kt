package dev.taah.crewmate.backend.util.inner

import dev.taah.crewmate.backend.inner.objects.InnerNetObjects
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBufUtil
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

class GameDataUtil {
    companion object {
        fun handleGameData(buffer: PacketBuffer, room: GameRoom) {
            var hazel = HazelMessage.read(buffer)
            while (hazel != null) {
                when (hazel.getTag()) {
                    4 -> {
                        println("Game Data Spawn Message")
                        handleSpawn(hazel.payload!!, room)
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
                for (i in 0 until components) {
                    val netId = buffer.readPackedUInt32()
                    val hazel = HazelMessage.read(buffer)!!
                    println("Tag ${hazel.getTag()} net id $netId and length ${hazel.length}")
                    val obj = innerNetObjects.objects[i]
                    val innerNetObject = obj.primaryConstructor!!.call(netId.toInt(), ownerId)
                    CrewmateServer.LOGGER.debug("Spawning ${innerNetObject.javaClass.simpleName} with owner ID $ownerId")
                    if (hazel.length > 0) {
//                        println("buffer: ${ByteBufUtil.prettyHexDump(hazel.payload!!)}")
                        innerNetObject.deserialize(hazel.payload!!, room)
                    }
                    innerNetObject.processObject(room)
                }
            }
        }
    }
}