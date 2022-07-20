package dev.taah.crewmate.util.inner

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.concurrent.ThreadLocalRandom
import java.util.stream.Collectors
import java.util.stream.Stream

class GameCode {
    companion object {
        private val CHAR_SET = "QWXRTYLPESDFGHUJKZOCVBINMA".toCharArray()
        private val CHAR_MAP =
            intArrayOf(25, 21, 19, 10, 8, 11, 12, 13, 22, 15, 16, 6, 24, 23, 18, 7, 0, 3, 9, 4, 14, 20, 1, 2, 5, 17)

        fun generateCode(): GameCode {
            val builder = StringBuilder()
            for (i in 0 until 6) {
                builder.append(CHAR_SET[ThreadLocalRandom.current().nextInt(CHAR_SET.size)])
            }
            return GameCode(builder.toString())
        }
    }

    val codeString: String
    val codeInt: Int

    constructor(codeString: String) {
        this.codeString = codeString
        this.codeInt = codeToInt(codeString)
    }

    constructor(codeInt: Int) {
        this.codeInt = codeInt
        this.codeString = intToCode(codeInt)!!
    }

    fun intToCode(gameId: Int): String? {
        return if (gameId < -1) {
            // Version 2 codes will always be negative
            val firstTwo = (gameId and 0x3FF)
            var lastFour = (gameId shr 10 and 0xFFFFF)
            Stream.of(
                CHAR_SET[firstTwo % 26],
                CHAR_SET[firstTwo / 26],
                CHAR_SET[lastFour % 26],
                CHAR_SET[26.let { lastFour /= it; lastFour } % 26],
                CHAR_SET[26.let { lastFour /= it; lastFour } % 26],
                CHAR_SET[lastFour / 26 % 26]
            ).map { obj: Char? ->
                java.lang.String.valueOf(
                    obj
                )
            }.collect(Collectors.joining())
        } else {
            // Version 1 codes will always be positive
            String(
                ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(Math.toIntExact(gameId.toLong())).array(),
                StandardCharsets.UTF_8
            )
        }
    }

    @Throws(IllegalArgumentException::class)
    fun codeToInt(gameCode: String): Int {
        var gameCode = gameCode
        gameCode = gameCode.uppercase()
        require(gameCode.chars().noneMatch { character: Int ->
            !Character.isLetter(
                character
            )
        }) { "Invalid code, expected letters only: $gameCode" }
        if (gameCode.length == 4) {
            return ByteBuffer.wrap(gameCode.toByteArray()).order(ByteOrder.LITTLE_ENDIAN).int
        }
        require(gameCode.length == 6) { "Invalid code length, expected 4 or 6 characters: $gameCode" }
        val first = CHAR_MAP[gameCode[0].code - 65]
        val second = CHAR_MAP[gameCode[1].code - 65]
        val third = CHAR_MAP[gameCode[2].code - 65]
        val fourth = CHAR_MAP[gameCode[3].code - 65]
        val fifth = CHAR_MAP[gameCode[4].code - 65]
        val sixth = CHAR_MAP[gameCode[5].code - 65]
        val firstTwo = first + 26 * second and 0x3FF
        val lastFour = third + 26 * (fourth + 26 * (fifth + 26 * sixth))
        return firstTwo or (lastFour shl 10 and 0x3FFFFC00) or -0x80000000
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (!(other is GameCode)) {
            return false
        }
        return other.codeString.equals(this.codeString, true) && other.codeInt == this.codeInt
    }
}