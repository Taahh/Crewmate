package dev.taah.crewmate.core.protocol

import com.google.common.collect.Maps
import dev.taah.crewmate.backend.protocol.AbstractPacket
import dev.taah.crewmate.backend.protocol.option.DisconnectPacket
import dev.taah.crewmate.backend.protocol.option.HelloPacket
import dev.taah.crewmate.backend.protocol.option.NormalPacket
import dev.taah.crewmate.backend.protocol.option.ReliablePacket
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class ProtocolHandler {
    companion object {
        val PACKET_MAP: HashMap<Byte, KClass<AbstractPacket<*>>> = Maps.newHashMap()
    }

    constructor() {
        this.registerPacket(0x00, NormalPacket::class as KClass<AbstractPacket<*>>)
        this.registerPacket(0x08, HelloPacket::class as KClass<AbstractPacket<*>>)
        this.registerPacket(0x01, ReliablePacket::class as KClass<AbstractPacket<*>>)
        this.registerPacket(0x09, DisconnectPacket::class as KClass<AbstractPacket<*>>)
    }

    fun registerPacket(id: Byte, clazz: KClass<AbstractPacket<*>>) {
        PACKET_MAP[id] = clazz
    }

    fun <T : AbstractPacket<T>> getPacket(id: Byte, nonce: Int = -1): AbstractPacket<T>? {
        if (!PACKET_MAP.containsKey(id)) {
            return null
        }
        val clazz = PACKET_MAP[id]
        val packet = clazz!!.primaryConstructor!!.call(nonce)
        return packet as AbstractPacket<T>
    }

}