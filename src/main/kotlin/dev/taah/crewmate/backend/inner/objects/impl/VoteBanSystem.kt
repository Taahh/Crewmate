package dev.taah.crewmate.backend.inner.objects.impl

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import dev.taah.crewmate.backend.inner.objects.AbstractInnerNetObject
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class VoteBanSystem(override val netId: Int, override val ownerId: Int) : AbstractInnerNetObject() {
    val votes: HashMap<Int, Array<Int>> = Maps.newHashMap()
    override fun processObject(room: GameRoom) {
        room.spawnedObjects[this.netId] = this
    }

    override fun serialize(buffer: PacketBuffer) {

    }

    override fun deserialize(buffer: PacketBuffer, room: GameRoom) {
        val votes = buffer.readByte()
        for (i in 0 until votes) {
            val key = buffer.readInt32()
            val arr: ArrayList<Int>
            var add = false
            if (!this.votes.containsKey(key)) {
                add = true
                arr = Lists.newArrayList()
            } else {
                arr = this.votes[key]!!.toMutableList() as ArrayList<Int>
            }
            for (i in 0 until 3) {
                arr.add(buffer.readPackedInt32())
            }
            if (add) {
                this.votes[key] = arr.toArray() as Array<Int>
            }
        }
    }
}