package dev.taah.crewmate.backend.inner.objects.impl

import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.backend.util.Vector2
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class CustomNetworkTransform(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    override var initialState: Boolean = false

    var lastSequenceId: Int = 0
    var position: Vector2? = null
    var velocity: Vector2? = null

    override fun processObject(room: GameRoom) {
//        println("cnt: ${CrewmateServer.GSON.toJson(this)}")
        this.initialState = false
        room.connections[this.ownerId]!!.playerControl!!.customNetworkTransform = this
    }

    override fun serialize(buffer: PacketBuffer) {
        if (this.initialState) {
            buffer.writeUInt16(this.lastSequenceId)
            Vector2.writeVector2(buffer, this.position!!)
            Vector2.writeVector2(buffer, this.velocity!!)
            return
        }
        ++this.lastSequenceId
        buffer.writeUInt16(this.lastSequenceId)
        Vector2.writeVector2(buffer, this.position!!)
        Vector2.writeVector2(buffer, this.velocity!!)
    }

    override fun deserialize(hazelMessage: HazelMessage) {
        val buffer = hazelMessage.payload!!
        if (this.initialState) {
            this.lastSequenceId = buffer.readUInt16()
            this.position = Vector2.readVector2(buffer)
            this.velocity = Vector2.readVector2(buffer)
        } else {
            val newSid = buffer.readUInt16()
            if (!sidGreaterThan(newSid, this.lastSequenceId)) {
                return
            }
            this.lastSequenceId = newSid
            this.position = Vector2.readVector2(buffer)
            this.velocity = Vector2.readVector2(buffer)
        }
    }

    fun sidGreaterThan(newSid: Int, prevSid: Int): Boolean
    {
        var num = prevSid + Short.MAX_VALUE
        return if (prevSid < num) newSid in (prevSid + 1)..num else newSid > prevSid || newSid <= num
    }
}