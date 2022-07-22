package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.game.GameRoomChatEvent
import dev.taah.crewmate.backend.event.game.GameRoomVentEvent
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class EnterVentRpc() : AbstractMessage(0x00) {

    var targetNetId: Int? = null
    var ventId: Int? = null

    constructor(ventId: Int) : this() {
        this.ventId = ventId
    }

    override fun processObject(room: GameRoom) {
        EventManager.INSTANCE!!.callEvent(GameRoomVentEvent(room, this.ventId!!, room.getConnectionByPlayerPhysicsNetId(this.targetNetId!!)!!, true))
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.EnterVent.id)
        buffer.writePackedInt32(this.ventId!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.ventId = buffer.readPackedInt32()
    }
}