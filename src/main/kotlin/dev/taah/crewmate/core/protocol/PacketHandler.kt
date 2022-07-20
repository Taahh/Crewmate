package dev.taah.crewmate.core.protocol

import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import java.net.InetSocketAddress
import java.util.*

class PacketHandler : SimpleChannelInboundHandler<ByteBuf>() {
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: ByteBuf?) {
        val connection: PlayerConnection

        if (ctx!!.channel().hasAttr(PlayerConnection.CONNECTION_STRING)) {
            if (ctx.channel().attr(PlayerConnection.CONNECTION_STRING).get() == null)
            {
                return
            }
            connection = ctx.channel().attr(PlayerConnection.CONNECTION_STRING).get()
        } else {
            connection = PlayerConnection(ctx!!, UUID.randomUUID())
            ctx.channel().attr(PlayerConnection.CONNECTION_STRING).set(connection)
            CrewmateServer.CONNECTIONS[ctx.channel().remoteAddress() as InetSocketAddress] = connection
        }
        val buffer = PacketBuffer(msg!!)
        println(ByteBufUtil.prettyHexDump(buffer))
        val tag: Byte = buffer.readByte()
        val packet = CrewmateServer.HANDLER.getPacket<AbstractPacket<*>>(tag)

        System.out.printf(
            "Received packet from user %s with packet ID %s, and length %s%n",
            connection.clientName,
//            nonce,
            tag,
            buffer.readableBytes()
        )
        if (packet != null) {
            System.out.printf("Deserializing packet %s%n", packet.javaClass.simpleName)
            packet.deserialize(buffer)
            packet.processPacket(packet, connection)
        }

    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        if (cause != null) {
            cause!!.printStackTrace()
        }
    }
}