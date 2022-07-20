package dev.taah.crewmate.backend.util

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollDatagramChannel
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.DatagramPacket
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.unix.UnixChannelOption
import io.netty.util.internal.RecyclableArrayList
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

//TODO: support switching autoread
//TODO: add ability to use external event loop group and epoll enablement
class UDPServerChannel @JvmOverloads constructor(ioThreads: Int = 1) : AbstractServerChannel() {
    protected val group: EventLoopGroup
    protected val ioBootstraps: MutableList<Bootstrap> = ArrayList()
    protected val ioChannels: MutableList<Channel> = ArrayList()
    protected val userChannels = ConcurrentHashMap<InetSocketAddress, UDPChannel>()

    @Sharable
    protected inner class ReadRouteChannelHandler : SimpleChannelInboundHandler<DatagramPacket>() {
        @Throws(Exception::class)
        override fun channelRead0(ctx: ChannelHandlerContext, p: DatagramPacket) {
            val channel = userChannels.compute(p.sender()) { lAddr: InetSocketAddress, lChannel: UDPChannel? ->
                if (lChannel == null || !lChannel.isOpen) UDPChannel(
                    this@UDPServerChannel,
                    lAddr
                ) else lChannel
            }
            channel!!.buffers.add(p.content().retain())
            if (channel.getIsNew()) {
                val serverPipeline = pipeline()
                serverPipeline.fireChannelRead(channel)
                serverPipeline.fireChannelReadComplete()
            } else {
                if (channel.isRegistered) {
                    channel.read()
                }
            }
        }
    }

    fun doWrite(list: RecyclableArrayList, remote: InetSocketAddress) {
        val ioChannel = ioChannels[remote.hashCode() and ioChannels.size - 1]
        ioChannel.eventLoop().execute {
            try {
                for (buf in list) {
                    ioChannel.write(DatagramPacket(buf as ByteBuf, remote))
                }
                ioChannel.flush()
            } finally {
                list.recycle()
            }
        }
    }

    fun doUserChannelRemove(userChannel: UDPChannel) {
        userChannels.compute(userChannel.remoteAddress() as InetSocketAddress) { lAddr: InetSocketAddress?, lChannel: UDPChannel? -> if (lChannel === userChannel) null else lChannel }
    }

    @Volatile
    protected var open = true
    override fun isOpen(): Boolean {
        return open
    }

    override fun isActive(): Boolean {
        return isOpen
    }

    @Throws(Exception::class)
    override fun doClose() {
        open = false
        ArrayList(userChannels.values).forEach(Consumer { obj: UDPChannel -> obj.close() })
        ioChannels.forEach(Consumer { obj: Channel -> obj.close() })
        group.shutdownGracefully().sync()
    }

    public override fun localAddress0(): SocketAddress?{
        return if (ioChannels.size > 0) ioChannels[0].localAddress() else null
    }

    override fun localAddress(): InetSocketAddress? {
        return if (ioChannels.size > 0) (ioChannels[0].localAddress() as InetSocketAddress) else null
    }

    @Throws(Exception::class)
    override fun doBind(local: SocketAddress) {
        for (bootstrap in ioBootstraps) {
            ioChannels.add(bootstrap.bind(local).sync().channel())
        }
        ioBootstraps.clear()
    }

    protected val config: DefaultChannelConfig = object : DefaultChannelConfig(this) {
        init {
            setRecvByteBufAllocator(FixedRecvByteBufAllocator(2048))
        }

        override fun isAutoRead(): Boolean {
            return true
        }

        override fun setAutoRead(autoRead: Boolean): ChannelConfig {
            return this
        }
    }

    init {
        var ioThreads = ioThreads
        require(ioThreads >= 1) { "IO threads cound can't be less than 1" }
        val epollAvailabe = Epoll.isAvailable()
        if (!epollAvailabe) {
            ioThreads = 1
        }
        group = if (epollAvailabe) EpollEventLoopGroup(ioThreads) else NioEventLoopGroup(ioThreads)
        val channel: Class<out DatagramChannel?> =
            if (epollAvailabe) EpollDatagramChannel::class.java else NioDatagramChannel::class.java
        val initializer: ChannelInitializer<Channel> = object : ChannelInitializer<Channel>() {
            val ioReadRoute = ReadRouteChannelHandler()
            @Throws(Exception::class)
            override fun initChannel(ioChannel: Channel) {
                ioChannel.pipeline().addLast(ioReadRoute)
            }
        }
        while (ioThreads-- > 0) {
            val ioBootstrap = Bootstrap().group(group).channel(channel).handler(initializer)
            if (epollAvailabe) {
                ioBootstrap.option(UnixChannelOption.SO_REUSEPORT, true)
            }
            ioBootstraps.add(ioBootstrap)
        }
    }

    override fun config(): DefaultChannelConfig {
        return config
    }

    override fun remoteAddress(): InetSocketAddress? {
        return null
    }

    override fun isCompatible(loop: EventLoop): Boolean {
        return true
    }

    @Throws(Exception::class)
    override fun doBeginRead() {
    }
}