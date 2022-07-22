package dev.taah.crewmate.backend.protocol.option

import com.google.common.collect.Maps
import dev.taah.crewmate.api.inner.enums.QuickChatMode
import dev.taah.crewmate.backend.connection.PlayerConnection
import dev.taah.crewmate.backend.inner.data.PlatformData
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.root.*
import dev.taah.crewmate.core.CrewmateServer
import dev.taah.crewmate.core.protocol.ProtocolHandler
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

open class ReliablePacket(nonce: Int) :
    AbstractPacket<ReliablePacket>(0x01, nonce) {

    companion object {
        val RELIABLE_PACKETS: HashMap<Byte, KClass<AbstractPacket<*>>> = Maps.newHashMap();
        fun registerPacket(id: Byte, clazz: KClass<AbstractPacket<*>>) {
            RELIABLE_PACKETS[id] = clazz;
        }

        fun <T : AbstractPacket<T>> getPacket(id: Byte, nonce: Int = -1): AbstractPacket<T>? {
            if (!RELIABLE_PACKETS.containsKey(id)) {
                return null;
            }
            val clazz = RELIABLE_PACKETS[id];
            val packet = clazz!!.primaryConstructor!!.call(nonce);
            return packet as AbstractPacket<T>
        }
    }

    var reliablePacket: AbstractPacket<*>? = null

    var payload: HazelMessage? = null

    init {
        registerPacket(0x00, HostGamePacket::class as KClass<AbstractPacket<*>>)
        registerPacket(0x01, JoinGamePacket::class as KClass<AbstractPacket<*>>)
        registerPacket(0x02, StartGamePacket::class as KClass<AbstractPacket<*>>)
        registerPacket(0x05, GameDataPacket::class as KClass<AbstractPacket<*>>)
        registerPacket(0x06, GameDataToPacket::class as KClass<AbstractPacket<*>>)
        registerPacket(0x07, JoinedGamePacket::class as KClass<AbstractPacket<*>>)
        registerPacket(0x08, EndGamePacket::class as KClass<AbstractPacket<*>>)
        registerPacket(10, AlterGamePacket::class as KClass<AbstractPacket<*>>)
    }

    override fun processPacket(packet: ReliablePacket, connection: PlayerConnection) {
        connection.sendAck(this.nonce)
        if (this.payload != null) {
            val packet = getPacket<AbstractPacket<*>>(this.payload!!.getTag().toByte())!!
            packet.deserialize(this.payload!!.payload!!)
            packet.processPacket(packet, connection)
        }
    }

    override fun serialize(buffer: PacketBuffer) {
        if (reliablePacket != null)
        {
            reliablePacket!!.serialize(buffer)
        }
    }

    override fun deserialize(buffer: PacketBuffer) {
        this.nonce = buffer.readUnsignedShort()
        val hazel = HazelMessage.read(buffer)!!
        if (RELIABLE_PACKETS.containsKey(hazel.getTag().toByte())) {
//            println("Deserializing Reliable Packet with tag ${hazel.getTag()}")
            this.payload = hazel
        } else {
//            println("Unknown Reliable Packet with tag ${hazel.getTag()}")
        }
    }



}