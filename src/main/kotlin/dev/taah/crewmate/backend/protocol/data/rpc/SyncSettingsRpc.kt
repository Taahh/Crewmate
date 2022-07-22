package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SyncSettingsRpc : AbstractMessage(0x00) {

    var gameOptionsData: GameOptionsData? = null

    override fun processObject(room: GameRoom) {
//        room.gameOptionsData = this.gameOptionsData
        println("room options: ${CrewmateServer.GSON.toJson(room.gameOptionsData!!)}")
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SyncSettings.id)
        this.gameOptionsData!!.serialize(buffer)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.gameOptionsData = GameOptionsData().deserialize(buffer) as GameOptionsData
    }

    fun gameOptionsData(gameOptionsData: GameOptionsData): SyncSettingsRpc {
       this.gameOptionsData = gameOptionsData
        return this
    }
}