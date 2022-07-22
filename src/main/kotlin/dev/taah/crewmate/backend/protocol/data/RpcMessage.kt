package dev.taah.crewmate.backend.protocol.data

import dev.taah.crewmate.backend.protocol.data.rpc.RpcFlags
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.full.createInstance

class RpcMessage : AbstractMessage(0x02) {

    private var remainingBuffer: PacketBuffer? = null
    private var rpc: AbstractMessage? = null
    var targetNetId: Int = 0
    var rpcFlag: RpcFlags? = null

    override fun processObject(room: GameRoom) {
        rpcFlag?.objects?.forEach {
            val rpc = it.createInstance()
//            if (rpc.javaClass.fields.any { it.name.equals("targetNetId", true) }) {
//                val field = rpc.javaClass.getDeclaredField("targetNetId")
//                field.isAccessible = true
//                field.set(rpc, this.targetNetId)
//            }
            rpc.deserialize(this.remainingBuffer!!)
            rpc.processObject(room)
        }
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
        this.rpcFlag = RpcFlags.getById(buffer.readByte().toInt())
        this.remainingBuffer = buffer
        println("RPC: ${rpcFlag?.name}")
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