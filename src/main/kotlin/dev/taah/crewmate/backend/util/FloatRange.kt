package dev.taah.crewmate.backend.util

import lombok.Data

@Data
class FloatRange(val min: Float = 0f, val max: Float = 0f) {
    /*
     * Used when reading a Vector2
     */
    fun lerp(value: Float): Float {
        var value = value
        if (0.0 > value) {
            value = 0.0f
        } else if (1.0 < value) {
            value = 1.0f
        }
        return min + ((max - min) * value)
    }

    /*
     * Used when writing a Vector2
     */
    fun reverseLerp(value: Float): Float {
        var value = value
        value = (value - min) / (max - min)
        if (0.0 > value) {
            value = 0.0f
        } else if (1.0 < value) {
            value = 1.0f
        }
        return value
    }
}