package dev.taah.crewmate.backend.util.inner

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.inner.objects.impl.*
import dev.taah.crewmate.backend.protocol.data.DataMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.data.SceneChangeMessage
import dev.taah.crewmate.backend.protocol.data.SpawnMessage
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class GameDataUtil {

    companion object {
        val INNER_OBJECTS: HashMap<Int, Array<KClass<out AbstractInnerNetObject>>> = Maps.newHashMap();

        init {
            registerInner(2, LobbyBehavior::class)
            registerInner(3, GameData::class, VoteBanSystem::class)
            registerInner(4, PlayerControl::class, PlayerPhysics::class, CustomNetworkTransform::class)
        }
        fun registerInner(id: Int, vararg clazz: KClass<out AbstractInnerNetObject>) {
            INNER_OBJECTS[id] = clazz.toList().toTypedArray()
        }

        fun getInner(id: Int): ArrayList<KClass<out AbstractInnerNetObject>>? {
            if (!INNER_OBJECTS.containsKey(id)) {
                return null;
            }
            val array: ArrayList<AbstractInnerNetObject> = Lists.newArrayList()
            val clazz = INNER_OBJECTS[id];
            return ArrayList(clazz!!.toList())
        }
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