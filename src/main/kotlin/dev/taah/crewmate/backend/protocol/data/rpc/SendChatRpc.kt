package dev.taah.crewmate.backend.protocol.data.rpc

import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.backend.event.game.GameRoomChatEvent
import dev.taah.crewmate.backend.inner.game.GameOptionsData
import dev.taah.crewmate.backend.protocol.data.AbstractMessage
import dev.taah.crewmate.backend.protocol.data.RpcMessage
import dev.taah.crewmate.backend.protocol.root.GameDataPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer

class SendChatRpc() : AbstractMessage() {

    var targetNetId: Int? = null
    var chatMessage: String? = null

    constructor(chatMessage: String) : this() {
        this.chatMessage = chatMessage
    }

    override fun processObject(room: GameRoom) {
//        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl!!.rpcSetName(this.name!!)
        room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!.playerControl?.rpcSendChat(this.chatMessage!!)
        EventManager.INSTANCE!!.callEvent(GameRoomChatEvent(room, this.chatMessage!!, room.getConnectionByPlayerControlNetId(this.targetNetId!!)!!))
    }

    override fun serialize(buffer: PacketBuffer) {
        buffer.writeByte(RpcFlags.SendChat.id)
        buffer.writePackedString(this.chatMessage!!)
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.chatMessage = buffer.readPackedString()
    }
}