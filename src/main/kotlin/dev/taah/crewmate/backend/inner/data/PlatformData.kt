package dev.taah.crewmate.backend.inner.data

import dev.taah.crewmate.api.inner.IPlatformData
import dev.taah.crewmate.util.HazelMessage
import dev.taah.crewmate.util.PacketBuffer
import io.netty.buffer.ByteBufUtil

class PlatformData : IPlatformData {
    override var platform: Byte = 0
    override var platformName: String = ""

    constructor(platform: Byte, platformName: String) {
        this.platform = platform
        this.platformName = platformName
    }

    constructor()
    
    override fun serialize(buffer: PacketBuffer) {
        val hazel = HazelMessage.start(this.platform.toInt());
        hazel.payload!!.writePackedString(this.platformName)
        hazel.endMessage()
        hazel.copyTo(buffer)
    }

    override fun deserialize(buffer: PacketBuffer): IPlatformData {
        val hazel = HazelMessage.read(buffer)
        return PlatformData(hazel!!.getTag().toByte(), hazel.payload!!.readPackedString())
    }
}