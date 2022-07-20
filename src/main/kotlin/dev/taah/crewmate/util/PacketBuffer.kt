package dev.taah.crewmate.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.Unpooled
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ByteProcessor
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.BiConsumer

/**
 * @author Phoenixx
 * RaptureAPI
 * 2020-11-16
 * 11:07 p.m.
 */
class PacketBuffer @JvmOverloads constructor(val byteBuf: ByteBuf = Unpooled.directBuffer(1)) : ByteBuf() {

    val byteArray: ByteArray
        get() {
            if (byteBuf.hasArray()) {
                return byteBuf.array()
            }
            val bytes = ByteArray(byteBuf.readableBytes())
            byteBuf.getBytes(byteBuf.readerIndex(), bytes)
            return bytes
        }
    val byteArraySafe: ByteArray
        get() = getByteArraySafe(byteBuf)

    fun writeByteArray(array: ByteArray): PacketBuffer {
        writeVarInt(array.size)
        this.writeBytes(array)
        return this
    }

    @JvmOverloads
    fun readByteArray(maxLength: Int = readableBytes()): ByteArray {
        val i = readVarInt()
        return if (i > maxLength) {
            throw DecoderException("ByteArray with size $i is bigger than allowed $maxLength")
        } else {
            val abyte = ByteArray(i)
            this.readBytes(abyte)
            abyte
        }
    }

    /**
     * Writes an array of VarInts to the buffer, prefixed by the length of the array (as a VarInt).
     */
    fun writeVarIntArray(array: IntArray): PacketBuffer {
        writeVarInt(array.size)
        for (i in array) {
            writeVarInt(i)
        }
        return this
    }

    @JvmOverloads
    fun readVarIntArray(maxLength: Int = readableBytes()): IntArray {
        val i = readVarInt()
        return if (i > maxLength) {
            throw DecoderException("VarIntArray with size $i is bigger than allowed $maxLength")
        } else {
            val aint = IntArray(i)
            for (j in aint.indices) {
                aint[j] = readVarInt()
            }
            aint
        }
    }

    /**
     * Writes an array of longs to the buffer, prefixed by the length of the array (as a VarInt).
     */
    fun writeLongArray(array: LongArray): PacketBuffer {
        writeVarInt(array.size)
        for (i in array) {
            writeLong(i)
        }
        return this
    }

    /**
     * Reads a length-prefixed array of longs from the buffer.
     */
    @JvmOverloads
    fun readLongArray(array: LongArray?, maxLength: Int = readableBytes() / 8): LongArray {
        var array = array
        val i = readVarInt()
        if (array == null || array.size != i) {
            if (i > maxLength) {
                throw DecoderException("LongArray with size $i is bigger than allowed $maxLength")
            }
            array = LongArray(i)
        }
        for (j in array.indices) {
            array[j] = readLong()
        }
        return array
    }

    fun <T : Enum<T>?> readEnumValue(enumClass: Class<T>): T {
        return enumClass.enumConstants[readVarInt()] as T
    }

    fun writeEnumValue(value: Enum<*>): PacketBuffer {
        return writeVarInt(value.ordinal)
    }

    fun writeUUID(pUuid: UUID): PacketBuffer {
        writeLong(pUuid.mostSignificantBits)
        writeLong(pUuid.leastSignificantBits)
        return this
    }

    fun <T> writeCollection(pCollection: Collection<T>, pElementWriter: BiConsumer<PacketBuffer?, T>) {
        writeVarInt(pCollection.size)
        for (t in pCollection) {
            pElementWriter.accept(this, t)
        }
    }

    /**
     * Reads a UUID encoded as two longs from this buffer.
     *
     * @see .writeUUID
     */
    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }

    /**
     * Reads a compressed int from the buffer. To do so it maximally reads 5 byte-sized chunks whose most significant bit
     * dictates whether another byte should be read.
     */
    fun readVarInt(): Int {
        var i = 0
        var j = 0
        while (true) {
            val b0 = readByte()
            i = i or (b0.toInt() and 127 shl j++) * 7
            if (j > 5) {
                throw RuntimeException("VarInt too big")
            }
            if (b0.toInt() and 128 != 128) {
                break
            }
        }
        return i
    }

    fun readVarLong(): Long {
        var i = 0L
        var j = 0
        while (true) {
            val b0 = readByte()
            i = i or ((b0.toInt() and 127).toLong() shl j++) * 7
            if (j > 10) {
                throw RuntimeException("VarLong too big")
            }
            if (b0.toInt() and 128 != 128) {
                break
            }
        }
        return i
    }

    fun writeUniqueId(uuid: UUID): PacketBuffer {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
        return this
    }

    fun readUniqueId(): UUID {
        return UUID(readLong(), readLong())
    }

    /**
     * Writes a compressed int to the buffer. The smallest number of bytes to fit the passed int will be written. Of each
     * such byte only 7 bits will be used to describe the actual value since its most significant bit dictates whether
     * the next byte is part of that same int. Micro-optimization for int values that are expected to have values below
     * 128.
     */
    fun writeVarInt(input: Int): PacketBuffer {
        var input = input
        while (input and -128 != 0) {
            writeByte(input and 127 or 128)
            input = input ushr 7
        }
        writeByte(input)
        return this
    }

    fun writeVarLong(value: Long): PacketBuffer {
        var value = value
        while (value and -128L != 0L) {
            writeByte((value and 127L).toInt() or 128)
            value = value ushr 7
        }
        writeByte(value.toInt())
        return this
    }

    /**
     * Reads a string from this buffer. Expected parameter is maximum allowed string length. Will throw IOException if
     * string length exceeds this value!
     */
    @JvmOverloads
    fun readString(maxLength: Int = 32767): String {
        val i = readVarInt()
        return if (i > maxLength * 4) {
            throw DecoderException("The received encoded string buffer length is longer than maximum allowed ($i > ${maxLength * 4})")
        } else if (i < 0) {
            throw DecoderException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val s = this.toString(this.readerIndex(), i, StandardCharsets.UTF_8)
            this.readerIndex(this.readerIndex() + i)
            if (s.length > maxLength) {
                throw DecoderException("The received string length is longer than maximum allowed ($i > $maxLength)")
            } else {
                s
            }
        }
    }

    @JvmOverloads
    fun writeString(string: String, maxLength: Int = 32767): PacketBuffer {
        val abyte = string.toByteArray(StandardCharsets.UTF_8)
        return if (abyte.size > maxLength) {
            throw EncoderException("String too big (was " + abyte.size + " bytes encoded, max " + maxLength + ")")
        } else {
            writeVarInt(abyte.size)
            this.writeBytes(abyte)
            this
        }
    }
    fun writePackedString(string: String, maxLength: Int = 32767): PacketBuffer {
        val abyte = string.toByteArray(StandardCharsets.UTF_8)
        return if (abyte.size > maxLength) {
            throw EncoderException("String too big (was " + abyte.size + " bytes encoded, max " + maxLength + ")")
        } else {
            writePackedUInt32(abyte.size.toLong())
            this.writeBytes(abyte)
            this
        }
    }

    fun readTime(): Date {
        return Date(readLong())
    }

    fun writeTime(time: Date): PacketBuffer {
        writeLong(time.time)
        return this
    }

    override fun capacity(): Int {
        return byteBuf.capacity()
    }

    override fun capacity(newCapacity: Int): ByteBuf {
        return byteBuf.capacity(newCapacity)
    }

    override fun maxCapacity(): Int {
        return byteBuf.maxCapacity()
    }

    override fun alloc(): ByteBufAllocator {
        return byteBuf.alloc()
    }

    override fun order(): ByteOrder {
        return byteBuf.order()
    }

    override fun order(byteOrder: ByteOrder): ByteBuf {
        return byteBuf.order(byteOrder)
    }

    override fun unwrap(): ByteBuf {
        return byteBuf.unwrap()
    }

    override fun isDirect(): Boolean {
        return byteBuf.isDirect
    }

    override fun isReadOnly(): Boolean {
        return byteBuf.isReadOnly
    }

    override fun asReadOnly(): ByteBuf {
        return byteBuf.asReadOnly()
    }

    override fun readerIndex(): Int {
        return byteBuf.readerIndex()
    }

    override fun readerIndex(readerIndex: Int): ByteBuf {
        return byteBuf.readerIndex(readerIndex)
    }

    override fun writerIndex(): Int {
        return byteBuf.writerIndex()
    }

    override fun writerIndex(writerIndex: Int): ByteBuf {
        return byteBuf.writerIndex(writerIndex)
    }

    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf {
        return byteBuf.setIndex(readerIndex, writerIndex)
    }

    override fun readableBytes(): Int {
        return byteBuf.readableBytes()
    }

    override fun writableBytes(): Int {
        return byteBuf.writableBytes()
    }

    override fun maxWritableBytes(): Int {
        return byteBuf.maxWritableBytes()
    }

    override fun isReadable(): Boolean {
        return byteBuf.isReadable
    }

    override fun isReadable(size: Int): Boolean {
        return byteBuf.isReadable(size)
    }

    override fun isWritable(): Boolean {
        return byteBuf.isWritable
    }

    override fun isWritable(size: Int): Boolean {
        return byteBuf.isWritable(size)
    }

    override fun clear(): ByteBuf {
        return byteBuf.clear()
    }

    override fun markReaderIndex(): ByteBuf {
        return byteBuf.markReaderIndex()
    }

    override fun resetReaderIndex(): ByteBuf {
        return byteBuf.resetReaderIndex()
    }

    override fun markWriterIndex(): ByteBuf {
        return byteBuf.markWriterIndex()
    }

    override fun resetWriterIndex(): ByteBuf {
        return byteBuf.resetWriterIndex()
    }

    override fun discardReadBytes(): ByteBuf {
        return byteBuf.discardReadBytes()
    }

    override fun discardSomeReadBytes(): ByteBuf {
        return byteBuf.discardSomeReadBytes()
    }

    override fun ensureWritable(minWritableBytes: Int): ByteBuf {
        return byteBuf.ensureWritable(minWritableBytes)
    }

    override fun ensureWritable(minWritableBytes: Int, force: Boolean): Int {
        return byteBuf.ensureWritable(minWritableBytes, force)
    }

    override fun getBoolean(index: Int): Boolean {
        return byteBuf.getBoolean(index)
    }

    override fun getByte(index: Int): Byte {
        return byteBuf.getByte(index)
    }

    override fun getUnsignedByte(index: Int): Short {
        return byteBuf.getUnsignedByte(index)
    }

    override fun getShort(index: Int): Short {
        return byteBuf.getShort(index)
    }

    override fun getShortLE(index: Int): Short {
        return byteBuf.getShortLE(index)
    }

    override fun getUnsignedShort(index: Int): Int {
        return byteBuf.getUnsignedShort(index)
    }

    override fun getUnsignedShortLE(index: Int): Int {
        return byteBuf.getUnsignedShortLE(index)
    }

    override fun getMedium(index: Int): Int {
        return byteBuf.getMedium(index)
    }

    override fun getMediumLE(index: Int): Int {
        return byteBuf.getMediumLE(index)
    }

    override fun getUnsignedMedium(index: Int): Int {
        return byteBuf.getUnsignedMedium(index)
    }

    override fun getUnsignedMediumLE(index: Int): Int {
        return byteBuf.getUnsignedMediumLE(index)
    }

    override fun getInt(index: Int): Int {
        return byteBuf.getInt(index)
    }

    override fun getIntLE(index: Int): Int {
        return byteBuf.getIntLE(index)
    }

    override fun getUnsignedInt(index: Int): Long {
        return byteBuf.getUnsignedInt(index)
    }

    override fun getUnsignedIntLE(index: Int): Long {
        return byteBuf.getUnsignedIntLE(index)
    }

    override fun getLong(index: Int): Long {
        return byteBuf.getLong(index)
    }

    override fun getLongLE(index: Int): Long {
        return byteBuf.getLongLE(index)
    }

    override fun getChar(index: Int): Char {
        return byteBuf.getChar(index)
    }

    override fun getFloat(index: Int): Float {
        return byteBuf.getFloat(index)
    }

    override fun getDouble(index: Int): Double {
        return byteBuf.getDouble(index)
    }

    override fun getBytes(index: Int, dst: ByteBuf): ByteBuf {
        return byteBuf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteBuf, length: Int): ByteBuf {
        return byteBuf.getBytes(index, dst, length)
    }

    override fun getBytes(index: Int, dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf {
        return byteBuf.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteArray): ByteBuf {
        return byteBuf.getBytes(index, dst)
    }

    override fun getBytes(index: Int, dst: ByteArray, dstIndex: Int, length: Int): ByteBuf {
        return byteBuf.getBytes(index, dst, dstIndex, length)
    }

    override fun getBytes(index: Int, dst: ByteBuffer): ByteBuf {
        return byteBuf.getBytes(index, dst)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: OutputStream, length: Int): ByteBuf {
        return byteBuf.getBytes(index, out, length)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: GatheringByteChannel, length: Int): Int {
        return byteBuf.getBytes(index, out, length)
    }

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: FileChannel, position: Long, length: Int): Int {
        return byteBuf.getBytes(index, out, position, length)
    }

    override fun getCharSequence(index: Int, length: Int, charset: Charset): CharSequence {
        return byteBuf.getCharSequence(index, length, charset)
    }

    override fun setBoolean(index: Int, value: Boolean): ByteBuf {
        return byteBuf.setBoolean(index, value)
    }

    override fun setByte(index: Int, value: Int): ByteBuf {
        return byteBuf.setByte(index, value)
    }

    override fun setShort(index: Int, value: Int): ByteBuf {
        return byteBuf.setShort(index, value)
    }

    override fun setShortLE(index: Int, value: Int): ByteBuf {
        return byteBuf.setShortLE(index, value)
    }

    override fun setMedium(index: Int, value: Int): ByteBuf {
        return byteBuf.setMedium(index, value)
    }

    override fun setMediumLE(index: Int, value: Int): ByteBuf {
        return byteBuf.setMediumLE(index, value)
    }

    override fun setInt(index: Int, value: Int): ByteBuf {
        return byteBuf.setInt(index, value)
    }

    override fun setIntLE(index: Int, value: Int): ByteBuf {
        return byteBuf.setIntLE(index, value)
    }

    override fun setLong(index: Int, value: Long): ByteBuf {
        return byteBuf.setLong(index, value)
    }

    override fun setLongLE(index: Int, value: Long): ByteBuf {
        return byteBuf.setLongLE(index, value)
    }

    override fun setChar(index: Int, value: Int): ByteBuf {
        return byteBuf.setChar(index, value)
    }

    override fun setFloat(index: Int, value: Float): ByteBuf {
        return byteBuf.setFloat(index, value)
    }

    override fun setDouble(index: Int, value: Double): ByteBuf {
        return byteBuf.setDouble(index, value)
    }

    override fun setBytes(index: Int, src: ByteBuf): ByteBuf {
        return byteBuf.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteBuf, length: Int): ByteBuf {
        return byteBuf.setBytes(index, src, length)
    }

    override fun setBytes(index: Int, src: ByteBuf, srcIndex: Int, length: Int): ByteBuf {
        return byteBuf.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteArray): ByteBuf {
        return byteBuf.setBytes(index, src)
    }

    override fun setBytes(index: Int, src: ByteArray, srcIndex: Int, length: Int): ByteBuf {
        return byteBuf.setBytes(index, src, srcIndex, length)
    }

    override fun setBytes(index: Int, src: ByteBuffer): ByteBuf {
        return byteBuf.setBytes(index, src)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, inputStream: InputStream, length: Int): Int {
        return byteBuf.setBytes(index, inputStream, length)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: ScatteringByteChannel, length: Int): Int {
        return byteBuf.setBytes(index, `in`, length)
    }

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: FileChannel, position: Long, length: Int): Int {
        return byteBuf.setBytes(index, `in`, position, length)
    }

    override fun setZero(index: Int, length: Int): ByteBuf {
        return byteBuf.setZero(index, length)
    }

    override fun setCharSequence(index: Int, charSequence: CharSequence, charset: Charset): Int {
        return byteBuf.setCharSequence(index, charSequence, charset)
    }

    fun readInt16(): Short {
        return readShortLE()
    }

    fun readInt32(): Int {
        return readIntLE()
    }

    fun readUInt16(): Int {
        return readUnsignedShortLE()
    }

    fun readUInt32(): Long {
        return readUnsignedIntLE()
    }

    fun readPackedString(): String {
        val len = readPackedUInt32().toInt()
        val charSequence = byteBuf.readCharSequence(len, Charset.defaultCharset())
        return charSequence.toString()
    }

    fun readPackedInt32(): Int {
        return readPackedUInt32().toInt()
    }

    fun readPackedUInt32(): Long {
        var readMore = true
        var shift = 0
        var output: Long = 0
        while (readMore) {
            var b = byteBuf.readUnsignedByte().toLong()
            if (b >= 0x80) {
                readMore = true
                b = b xor 0x80L
            } else {
                readMore = false
            }
            output = output or (b shl shift)
            shift += 7
        }
        return output
    }

    override fun readBoolean(): Boolean {
        return byteBuf.readBoolean()
    }

    override fun readByte(): Byte {
        return byteBuf.readByte()
    }

    override fun readUnsignedByte(): Short {
        return byteBuf.readUnsignedByte()
    }

    override fun readShort(): Short {
        return byteBuf.readShort()
    }

    override fun readShortLE(): Short {
        return byteBuf.readShortLE()
    }

    override fun readUnsignedShort(): Int {
        return byteBuf.readUnsignedShort()
    }

    override fun readUnsignedShortLE(): Int {
        return byteBuf.readUnsignedShortLE()
    }

    override fun readMedium(): Int {
        return byteBuf.readMedium()
    }

    override fun readMediumLE(): Int {
        return byteBuf.readMediumLE()
    }

    override fun readUnsignedMedium(): Int {
        return byteBuf.readUnsignedMedium()
    }

    override fun readUnsignedMediumLE(): Int {
        return byteBuf.readUnsignedMediumLE()
    }

    override fun readInt(): Int {
        return byteBuf.readInt()
    }

    override fun readIntLE(): Int {
        return byteBuf.readIntLE()
    }

    override fun readUnsignedInt(): Long {
        return byteBuf.readUnsignedInt()
    }

    override fun readUnsignedIntLE(): Long {
        return byteBuf.readUnsignedIntLE()
    }

    override fun readLong(): Long {
        return byteBuf.readLong()
    }

    override fun readLongLE(): Long {
        return byteBuf.readLongLE()
    }

    override fun readChar(): Char {
        return byteBuf.readChar()
    }

    override fun readFloat(): Float {
        return byteBuf.readFloat()
    }

    override fun readDouble(): Double {
        return byteBuf.readDouble()
    }

    override fun readBytes(length: Int): ByteBuf {
        return byteBuf.readBytes(length)
    }

    override fun readSlice(length: Int): ByteBuf {
        return byteBuf.readSlice(length)
    }

    override fun readRetainedSlice(length: Int): ByteBuf {
        return byteBuf.readRetainedSlice(length)
    }

    override fun readBytes(dst: ByteBuf): ByteBuf {
        return byteBuf.readBytes(dst)
    }

    override fun readBytes(dst: ByteBuf, length: Int): ByteBuf {
        return byteBuf.readBytes(dst, length)
    }

    override fun readBytes(dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf {
        return byteBuf.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteArray): ByteBuf {
        return byteBuf.readBytes(dst)
    }

    override fun readBytes(dst: ByteArray, dstIndex: Int, length: Int): ByteBuf {
        return byteBuf.readBytes(dst, dstIndex, length)
    }

    override fun readBytes(dst: ByteBuffer): ByteBuf {
        return byteBuf.readBytes(dst)
    }

    @Throws(IOException::class)
    override fun readBytes(out: OutputStream, length: Int): ByteBuf {
        return byteBuf.readBytes(out, length)
    }

    @Throws(IOException::class)
    override fun readBytes(out: GatheringByteChannel, length: Int): Int {
        return byteBuf.readBytes(out, length)
    }

    override fun readCharSequence(length: Int, charset: Charset): CharSequence {
        return byteBuf.readCharSequence(length, charset)
    }

    @Throws(IOException::class)
    override fun readBytes(out: FileChannel, position: Long, length: Int): Int {
        return byteBuf.readBytes(out, position, length)
    }

    override fun skipBytes(position: Int): ByteBuf {
        return byteBuf.skipBytes(position)
    }

    override fun writeBoolean(value: Boolean): ByteBuf {
        return byteBuf.writeBoolean(value)
    }

    fun writeInt16(value: Short) {
        writeShortLE(value.toInt())
    }

    fun writeInt32(value: Int) {
        writeIntLE(value)
    }

    fun writePackedInt32(value: Int) {
        writePackedUInt32(Integer.toUnsignedLong(value))
    }

    fun writeUInt16(value: Int) {
        writeShortLE(value)
    }

    fun writeUInt32(value: Long) {
        writeLongLE(value)
    }

    fun writePackedUInt32(value: Long) {
        var value = value
        do {
            var b = value
            if (value >= 0x80 and 0xFF) {
                b = b or (0x80 and 0xFF).toLong()
            }
            writeByte(b.toShort().toInt())
            value = value shr 7
        } while (value > 0)
    }

    override fun writeByte(value: Int): ByteBuf {
        return byteBuf.writeByte(value)
    }

    override fun writeShort(value: Int): ByteBuf {
        return byteBuf.writeShort(value)
    }

    override fun writeShortLE(value: Int): ByteBuf {
        return byteBuf.writeShortLE(value)
    }

    override fun writeMedium(value: Int): ByteBuf {
        return byteBuf.writeMedium(value)
    }

    override fun writeMediumLE(value: Int): ByteBuf {
        return byteBuf.writeMediumLE(value)
    }

    override fun writeInt(value: Int): ByteBuf {
        return byteBuf.writeInt(value)
    }

    override fun writeIntLE(value: Int): ByteBuf {
        return byteBuf.writeIntLE(value)
    }

    override fun writeLong(value: Long): ByteBuf {
        return byteBuf.writeLong(value)
    }

    override fun writeLongLE(value: Long): ByteBuf {
        return byteBuf.writeLongLE(value)
    }

    override fun writeChar(value: Int): ByteBuf {
        return byteBuf.writeChar(value)
    }

    override fun writeFloat(value: Float): ByteBuf {
        return byteBuf.writeFloat(value)
    }

    override fun writeDouble(value: Double): ByteBuf {
        return byteBuf.writeDouble(value)
    }

    override fun writeBytes(src: ByteBuf): ByteBuf {
        return byteBuf.writeBytes(src)
    }

    override fun writeBytes(src: ByteBuf, length: Int): ByteBuf {
        return byteBuf.writeBytes(src, length)
    }

    override fun writeBytes(src: ByteBuf, srcIndex: Int, length: Int): ByteBuf {
        return byteBuf.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteArray): ByteBuf {
        return byteBuf.writeBytes(src)
    }

    override fun writeBytes(src: ByteArray, srcIndex: Int, length: Int): ByteBuf {
        return byteBuf.writeBytes(src, srcIndex, length)
    }

    override fun writeBytes(src: ByteBuffer): ByteBuf {
        return byteBuf.writeBytes(src)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: InputStream, length: Int): Int {
        return byteBuf.writeBytes(`in`, length)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: ScatteringByteChannel, length: Int): Int {
        return byteBuf.writeBytes(`in`, length)
    }

    @Throws(IOException::class)
    override fun writeBytes(`in`: FileChannel, position: Long, length: Int): Int {
        return byteBuf.writeBytes(`in`, position, length)
    }

    override fun writeZero(length: Int): ByteBuf {
        return byteBuf.writeZero(length)
    }

    override fun writeCharSequence(charSequence: CharSequence, charset: Charset): Int {
        return byteBuf.writeCharSequence(charSequence, charset)
    }

    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte): Int {
        return byteBuf.indexOf(fromIndex, toIndex, value)
    }

    override fun bytesBefore(value: Byte): Int {
        return byteBuf.bytesBefore(value)
    }

    override fun bytesBefore(length: Int, value: Byte): Int {
        return byteBuf.bytesBefore(length, value)
    }

    override fun bytesBefore(index: Int, length: Int, value: Byte): Int {
        return byteBuf.bytesBefore(index, length, value)
    }

    override fun forEachByte(processor: ByteProcessor): Int {
        return byteBuf.forEachByte(processor)
    }

    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor): Int {
        return byteBuf.forEachByte(index, length, processor)
    }

    override fun forEachByteDesc(processor: ByteProcessor): Int {
        return byteBuf.forEachByteDesc(processor)
    }

    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor): Int {
        return byteBuf.forEachByteDesc(index, length, processor)
    }

    override fun copy(): ByteBuf {
        return byteBuf.copy()
    }

    fun copyPacketBuffer(): PacketBuffer {
        return PacketBuffer(byteBuf.copy())
    }

    override fun copy(index: Int, length: Int): ByteBuf {
        return byteBuf.copy(index, length)
    }

    override fun slice(): ByteBuf {
        return byteBuf.slice()
    }

    override fun retainedSlice(): ByteBuf {
        return byteBuf.retainedSlice()
    }

    override fun slice(index: Int, length: Int): ByteBuf {
        return byteBuf.slice(index, length)
    }

    override fun retainedSlice(index: Int, length: Int): ByteBuf {
        return byteBuf.retainedSlice(index, length)
    }

    override fun duplicate(): ByteBuf {
        return byteBuf.duplicate()
    }

    override fun retainedDuplicate(): ByteBuf {
        return byteBuf.retainedDuplicate()
    }

    override fun nioBufferCount(): Int {
        return byteBuf.nioBufferCount()
    }

    override fun nioBuffer(): ByteBuffer {
        return byteBuf.nioBuffer()
    }

    override fun nioBuffer(index: Int, length: Int): ByteBuffer {
        return byteBuf.nioBuffer(index, length)
    }

    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer {
        return byteBuf.internalNioBuffer(index, length)
    }

    override fun nioBuffers(): Array<ByteBuffer> {
        return byteBuf.nioBuffers()
    }

    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> {
        return byteBuf.nioBuffers(index, length)
    }

    override fun hasArray(): Boolean {
        return byteBuf.hasArray()
    }

    override fun array(): ByteArray {
        return byteBuf.array()
    }

    override fun arrayOffset(): Int {
        return byteBuf.arrayOffset()
    }

    override fun hasMemoryAddress(): Boolean {
        return byteBuf.hasMemoryAddress()
    }

    override fun memoryAddress(): Long {
        return byteBuf.memoryAddress()
    }

    override fun toString(charset: Charset): String {
        return byteBuf.toString(charset)
    }

    override fun toString(index: Int, length: Int, charset: Charset): String {
        return byteBuf.toString(index, length, charset)
    }

    override fun hashCode(): Int {
        return byteBuf.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        return byteBuf == obj
    }

    override fun compareTo(buffer: ByteBuf): Int {
        return byteBuf.compareTo(buffer)
    }

    override fun toString(): String {
        return byteBuf.toString()
    }

    override fun retain(increment: Int): ByteBuf {
        return byteBuf.retain(increment)
    }

    override fun retain(): ByteBuf {
        return byteBuf.retain()
    }

    override fun touch(): ByteBuf {
        return byteBuf.touch()
    }

    override fun touch(hint: Any): ByteBuf {
        return byteBuf.touch(hint)
    }

    override fun refCnt(): Int {
        return byteBuf.refCnt()
    }

    override fun release(): Boolean {
        return byteBuf.release()
    }

    override fun release(decrement: Int): Boolean {
        return byteBuf.release(decrement)
    }

    companion object {
        /**
         * Calculates the number of bytes required to fit the supplied int (0-5) if it were to be read/written using
         * readVarIntFromBuffer or writeVarIntToBuffer
         */
        fun getVarIntSize(input: Int): Int {
            for (i in 1..4) {
                if (input and (-1 shl i) * 7 == 0) {
                    return i
                }
            }
            return 5
        }

        fun getByteArraySafe(byteBuf: ByteBuf): ByteArray {
            if (byteBuf.hasArray()) {
                return byteBuf.array()
            }
            val indexTemp = byteBuf.readerIndex()
            byteBuf.readerIndex(0)
            val bytes = ByteArray(byteBuf.readableBytes())
            byteBuf.getBytes(byteBuf.readerIndex(), bytes)
            byteBuf.readerIndex(indexTemp)
            return bytes
        }
    }
}