package dev.taah.crewmate.backend.protocol.data

import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class SceneChangeMessage : AbstractMessage(0x06) {

    var clientId: Int = 0
    var targetScene: String? = null

    override fun processObject(room: GameRoom) {
//        room.broadcastReliablePacket(GameDataPacket(-1).addMessage(this).gameCode(room.gameCode), this.clientId)
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x06)
        hazel.payload!!.writePackedInt32(this.clientId)
        hazel.payload!!.writePackedString(this.targetScene!!)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.clientId = buffer.readPackedInt32()
        this.targetScene = buffer.readPackedString()
        println("Target $clientId changing to $targetScene")
    }

}