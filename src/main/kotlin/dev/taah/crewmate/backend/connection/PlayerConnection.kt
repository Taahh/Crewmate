package dev.taah.crewmate.backend.connection

import dev.taah.crewmate.api.connection.IConnection
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import dev.taah.crewmate.api.inner.enums.QuickChatMode
import dev.taah.crewmate.backend.event.connection.GameRoomLeaveEvent
import dev.taah.crewmate.backend.inner.data.PlatformData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.AcknowledgementPacket
import dev.taah.crewmate.backend.protocol.option.DisconnectPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.room.GameRoom
import dev.taah.crewmate.util.PacketBuffer
import dev.taah.crewmate.util.inner.GameCode
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.util.AttributeKey
import java.util.*

class PlayerConnection(
    @Transient val channel: ChannelHandlerContext,
    override val uniqueId: UUID,
    override var clientName: String = ""
) : IConnection<AbstractPacket<*>> {

    private var nonce = 0

    var clientVersion: Int = 0
    var chatModeType: QuickChatMode = QuickChatMode.QuickChatOnly
    var platformData: PlatformData = PlatformData()
    var gameCode: GameCode? = null

    companion object {
        val CONNECTION_STRING: AttributeKey<PlayerConnection> = AttributeKey.newInstance("player_conn")
    }

    override fun sendPacket(packet: AbstractPacket<*>, nonce: Int) {
        val buffer = PacketBuffer()
        buffer.writeByte(packet.packetType.toInt())
        if (nonce != -1) {
            buffer.writeShort(nonce)
        }
        packet.serialize(buffer)
        channel.channel().writeAndFlush(buffer.copyPacketBuffer().retain()).addListener {
            ChannelFutureListener { future ->
                if (future.isSuccess) {
                    channel.channel().read()
                } else {
                    future.channel().close()
                }
            }
        }
        CrewmateServer.LOGGER!!.debug(
            "Sending packet to ${this.clientName}: ${if (packet is ReliablePacket) packet.reliablePacket!!.javaClass.simpleName else packet.javaClass.simpleName}"
        )
    }

    override fun sendReliablePacket(packet: AbstractPacket<*>) {
        val nonce = this.getNextNonce()
        sendPacket(packet, nonce)
    }

    private fun sendDisconnect(packet: DisconnectPacket) {
        sendPacket(packet)
       if (this.gameCode != null) {
           if (GameRoom.exists(this.gameCode!!)) {
               val room = GameRoom.get(this.gameCode!!)
               val player =
                   room.players.entries.filter { entry -> entry.value.uniqueId.equals(this.uniqueId) }.first()
               room.players.remove(player.key)
               this.gameCode = null
               EventManager.INSTANCE!!.callEvent(GameRoomLeaveEvent(this, room))
               channel.channel().attr(CONNECTION_STRING).set(null)
           }
       }
    }

    override fun sendDisconnect(disconnectReasons: DisconnectReasons, reason: String?) {
        val packet = DisconnectPacket(-1)
        packet.disconnectReasons = disconnectReasons
        packet.reason = reason
        sendDisconnect(packet)
    }

    fun getNextNonce(): Int {

        return ++this.nonce
    }

    fun sendAck(nonce: Int) {
        sendPacket(
            AcknowledgementPacket(
                nonce
            )
        )
    }
}