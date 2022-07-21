package dev.taah.crewmate.util

import lombok.Getter
import lombok.Setter

@Getter
@Setter
class HazelMessage {
    var length = 0
        private set
    private var tag = 0

    var payload: PacketBuffer? = null

    fun endMessage() {
        val length: Int = this.payload!!.readableBytes() - 3
        this.payload!!.markWriterIndex()
        this.payload!!.writerIndex(0)
        this.payload!!.writeUInt16(length)
        this.length = length;
        this.payload!!.resetWriterIndex()
    }

    fun copyTo(buffer: PacketBuffer) {
        buffer.writeBytes(this.payload!!)
    }

    fun getTag(): Int {
        return tag
    }

    private fun setTag(tag: Int) {
        this.tag = tag
    }

    companion object {
        fun read(buffer: PacketBuffer): HazelMessage? {
            val message = HazelMessage()
            try {
                message.length = buffer.readUInt16()
                message.tag = buffer.readUnsignedByte().toInt()
            } catch (e: IndexOutOfBoundsException) {
                return null
            }
            message.payload = buffer.copyPacketBuffer()
            buffer.readerIndex(buffer.readerIndex() + message.length)
            return message
        }

        private fun start(buffer: PacketBuffer, tag: Int): HazelMessage {
            val hazelMessage = HazelMessage()
            hazelMessage.tag = tag
            hazelMessage.payload = buffer
            buffer.writeUInt16(0x00)
            buffer.writeByte(tag)
            return hazelMessage
        }

        fun start(tag: Int): HazelMessage {
            return start(PacketBuffer(), tag)
        }
    }
}