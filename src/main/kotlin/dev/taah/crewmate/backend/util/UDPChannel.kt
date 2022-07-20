package dev.taah.crewmate.backend.util

import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.util.ReferenceCountUtil
import io.netty.util.internal.RecyclableArrayList
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

class UDPChannel(protected val serverChannel: UDPServerChannel, protected val remote: InetSocketAddress) :
    AbstractChannel(
        serverChannel
    ) {
    protected val metadata = ChannelMetadata(false)
    protected val config = DefaultChannelConfig(this)
    protected var isNew = AtomicBoolean(true)
    fun getIsNew(): Boolean {
        return isNew.compareAndSet(true, false)
    }

    override fun metadata(): ChannelMetadata {
        return metadata
    }

    override fun config(): ChannelConfig {
        return config
    }

    @Volatile
    protected var open = true
    override fun isActive(): Boolean {
        return open
    }

    override fun isOpen(): Boolean {
        return isActive
    }

    @Throws(Exception::class)
    override fun doClose() {
        open = false
        serverChannel.doUserChannelRemove(this)
    }

    @Throws(Exception::class)
    override fun doDisconnect() {
        doClose()
    }

    val buffers = ConcurrentLinkedQueue<ByteBuf?>()
    protected fun addBuffer(buffer: ByteBuf?) {
        buffers.add(buffer)
    }

    protected var reading = false
    @Throws(Exception::class)
    override fun doBeginRead() {
        if (reading) {
            return
        }
        reading = true
        try {
            var buffer: ByteBuf? = null
            while (buffers.poll().also { buffer = it } != null) {
                pipeline().fireChannelRead(buffer)
            }
            pipeline().fireChannelReadComplete()
        } finally {
            reading = false
        }
    }

    @Throws(Exception::class)
    override fun doWrite(buffer: ChannelOutboundBuffer) {
        val list = RecyclableArrayList.newInstance()
        var freeList = true
        try {
            var buf: ByteBuf? = null
            while ((buffer.current() as ByteBuf?). also { buf = it } != null) {
                list.add(buf!!.retain())
                buffer.remove()
            }
            freeList = false
        } finally {
            if (freeList) {
                for (obj in list) {
                    ReferenceCountUtil.safeRelease(obj)
                }
                list.recycle()
            }
        }
        serverChannel.doWrite(list, remote)
    }

    override fun isCompatible(eventloop: EventLoop): Boolean {
        return true
    }

    override fun newUnsafe(): AbstractUnsafe {
        return object : AbstractUnsafe() {
            override fun connect(addr1: SocketAddress, addr2: SocketAddress, pr: ChannelPromise) {
                throw UnsupportedOperationException()
            }
        }
    }

    override fun localAddress0(): SocketAddress? {
        return serverChannel.localAddress0()
    }

    override fun remoteAddress0(): SocketAddress {
        return remote
    }

    @Throws(Exception::class)
    override fun doBind(addr: SocketAddress) {
        throw UnsupportedOperationException()
    }
}