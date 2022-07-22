package dev.taah.crewmate.backend.protocol.data

import com.google.common.collect.Maps
import dev.taah.crewmate.backend.protocol.data.rpc.RpcFlags
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class RpcMessage() : AbstractMessage(0x02) {
    companion object {
        val RPCS: HashMap<Int, KClass<out AbstractMessage>> = Maps.newHashMap();
        fun registerRpc(id: Int, clazz: KClass<out AbstractMessage>) {
            RPCS[id] = clazz
        }

        fun getRpc(id: Int): KClass<out AbstractMessage>? {
            if (!RPCS.containsKey(id)) {
                return null;
            }
            val clazz = RPCS[id];
            return clazz
        }
    }

    private var remainingBuffer: PacketBuffer? = null
    private var rpc: AbstractMessage? = null
    var targetNetId: Int = 0

    constructor(targetNetId: Int, rpc: AbstractMessage) : this() {
        this.targetNetId = targetNetId
        this.rpc = rpc
    }

    override fun processObject(room: GameRoom) {
        this.rpc?.deserialize(this.remainingBuffer!!)
        this.rpc?.processObject(room)
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x02)
        hazel.payload!!.writePackedUInt32(this.targetNetId.toLong())
        rpc!!.serialize(hazel.payload!!)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.targetNetId = buffer.readPackedUInt32().toInt()
        val callId = buffer.readUnsignedByte().toInt()
        var unknown = true
        if (RpcFlags.getById(callId) != null) {
            this.rpc = RpcFlags.getById(callId)?.clazz?.createInstance()
            unknown = false
        } else {
            if (getRpc(callId) != null) {
                this.rpc = getRpc(callId)?.createInstance()
                unknown = false
            }
        }
        this.remainingBuffer = buffer
        if (unknown) {
            CrewmateServer.LOGGER.debug("Unknown incoming RPC with call id $callId")
        } else {
            CrewmateServer.LOGGER.debug("Incoming RPC with call id $callId")
        }
    }

    fun rpc(rpc: AbstractMessage): RpcMessage {
        this.rpc = rpc
        return this
    }

    fun target(targetNetId: Int): RpcMessage {
        this.targetNetId = targetNetId
        return this
    }

}