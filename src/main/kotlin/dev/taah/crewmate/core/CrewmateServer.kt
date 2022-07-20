package dev.taah.crewmate.core

import com.google.common.collect.Maps
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.taah.crewmate.api.event.EventManager
import dev.taah.crewmate.api.inner.enums.DisconnectReasons
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.util.UDPServerChannel
import dev.taah.crewmate.core.protocol.PacketHandler
import dev.taah.crewmate.core.protocol.ProtocolHandler
import dev.taah.crewmate.core.test.TestEvent
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import java.io.File
import java.net.InetSocketAddress

fun setup()
{
    var plugins = File("plugins" + File.separator)
    if (!plugins.exists()) {
        plugins.mkdir()
    }
}

fun main() {
    setup()
    EventManager.INSTANCE = EventManager()
    EventManager.INSTANCE!!.registerEvent(TestEvent())

    Runtime.getRuntime().addShutdownHook(object: Thread(){
        override fun run() {
            CrewmateServer.CONNECTIONS.values.forEach { it.sendDisconnect(DisconnectReasons.Custom, "Crewmate was shut down!") }
        }
    })

    val bootstrap = ServerBootstrap()
        .group(DefaultEventLoopGroup())
        .childHandler(PacketHandler())
        .childHandler(object : ChannelInitializer<Channel>() {
            override fun initChannel(channel: Channel) {
                channel.pipeline()
//                    .addLast(ReadTimeoutHandler(10))
                    .addLast(PacketHandler())
                    .addLast(object : ChannelDuplexHandler() {
                        @Throws(Exception::class)
                        override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
                            super.write(ctx, msg, promise)
                        }
                    })
            }
        })
    bootstrap.channel(UDPServerChannel::class.java)
    bootstrap.bind(22023).syncUninterruptibly()
}

class CrewmateServer {
    companion object {
        val GSON: Gson = GsonBuilder().setPrettyPrinting().create()
        val CONNECTIONS: HashMap<InetSocketAddress, PlayerConnection> = Maps.newHashMap()
        val HANDLER: ProtocolHandler = ProtocolHandler()
        var INSTANCE: CrewmateServer? = null
    }
}