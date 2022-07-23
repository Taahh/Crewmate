package dev.taah.crewmate.backend.protocol.data

import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.backend.protocol.root.GameDataToPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer

class SceneChangeMessage : AbstractMessage() {

    var clientId: Int = 0
    var targetScene: String? = null

    var buffer: PacketBuffer? = null

    override fun processObject(room: GameRoom) {
        if (this.target != null) {
            CrewmateServer.LOGGER.debug("TARGET WAS NOT NULL, SENDING GAME DATA TO FOR SCENE CHANGE")
            room.connections[this.target]?.sendReliablePacket(GameDataToPacket().target(this.target!!).gameCode(room.gameCode).addMessage(this))
        } else {
            CrewmateServer.LOGGER.debug("SENDING GAME DATA PAKCET FOR SCENE CHANGE")
            if (sender != null) {
                CrewmateServer.LOGGER.debug("SENDER WAS NOT NULL")
                val entry = room.connections.entries.find { entry -> entry.value.uniqueId.equals(this.sender!!.uniqueId) }
                room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(this), entry!!.key)
            } else {
                CrewmateServer.LOGGER.debug("SENDER WAS NULL")
                room.broadcastReliablePacket(GameDataPacket().gameCode(room.gameCode).addMessage(this))
            }
        }

        if (this.targetScene != null && this.targetScene!!.equals("OnlineGame", true)) {

        } else {
            CrewmateServer.LOGGER.debug("Changed scene to ${this.targetScene!!}")
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(0x06)
        if (this.buffer != null) {
            hazel.payload!!.writeBytes(this.buffer!!)
        } else {
            hazel.payload!!.writePackedInt32(this.clientId)
            hazel.payload!!.writePackedString(this.targetScene!!)
        }
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
//        this.buffer = buffer.copyPacketBuffer()
        this.clientId = buffer.readPackedInt32()
        this.targetScene = buffer.readPackedString()
        println("Target $clientId changing to $targetScene")
    }

}