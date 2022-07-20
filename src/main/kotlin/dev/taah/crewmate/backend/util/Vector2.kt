package dev.taah.crewmate.backend.util

import dev.taah.crewmate.util.PacketBuffer
import lombok.Data

@Data
class Vector2(val x: Float = 0f, val y: Float = 0f) {
    companion object {
        private val RANGE_X = FloatRange(-50.0f, 50.0f)
        private val RANGE_Y = FloatRange(-50.0f, 50.0f)
        fun readVector2(reader: PacketBuffer): Vector2 {
            val x = reader.readUInt16() as Float / 65535.0f
            val y = reader.readUInt16() as Float / 65535.0f
            return Vector2(RANGE_X.lerp(x), RANGE_Y.lerp(y))
        }

        fun writeVector2(writer: PacketBuffer, vec: Vector2) {
            // These are uint16, but we declare them as an int since Java has no unsigned primitives
            val x = (RANGE_X.reverseLerp(vec.x) * 65535.0f).toInt()
            val y = (RANGE_Y.reverseLerp(vec.y) * 65535.0f).toInt()
            writer.writeUInt16(x)
            writer.writeUInt16(y)
        }
    }
}