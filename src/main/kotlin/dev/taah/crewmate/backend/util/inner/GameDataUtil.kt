package dev.taah.crewmate.backend.util.inner

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.room.GameRoomDataEvent
import dev.taah.crewmate.backend.event.room.GameRoomSpawnEvent
import dev.taah.crewmate.backend.inner.objects.InnerNetObjects
import dev.taah.crewmate.backend.inner.objects.impl.GameData
import dev.taah.crewmate.backend.protocol.data.DataMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.data.SceneChangeMessage
import dev.taah.crewmate.backend.protocol.data.SpawnMessage
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import org.checkerframework.checker.guieffect.qual.UI
import kotlin.reflect.full.primaryConstructor

class GameDataUtil {
    companion object {
        fun handleGameData(buffer: PacketBuffer, room: GameRoom) {
            var hazel = HazelMessage.read(buffer)
            while (hazel != null) {
                when (hazel.getTag()) {
                    4 -> {
                        println("Game Data Spawn Message")
                        val spawnMessage = SpawnMessage(room)
                        spawnMessage.deserialize(hazel.payload!!)
                        spawnMessage.processObject(room)
                    }
                    1 -> {
                        println("Game Data Data Message")
                        val dataMessage = DataMessage()
                        dataMessage.deserialize(hazel.payload!!)
                        dataMessage.processObject(room)
                    }
                    2 -> {
                        println("Game Data RPC Message")
                        val rpcMessage = RpcMessage()
                        rpcMessage.deserialize(hazel.payload!!)
                        rpcMessage.processObject(room)
                    }
                    6 -> {
                        println("Game Data Scene Change Message")
                        val sceneChangeMessage = SceneChangeMessage()
                        sceneChangeMessage.deserialize(hazel.payload!!)
                        sceneChangeMessage.processObject(room)
                    }
                    else -> {
                        CrewmateServer.LOGGER.warn("Unknown Game Data Flag: ${hazel.getTag()}")
                    }
                }
                hazel = HazelMessage.read(buffer)
            }
        }


    }
}