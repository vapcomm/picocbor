/*
 * (c) VAP Communications Group, 2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package online.vapcom.picocbor

import kotlin.math.absoluteValue

/**
 * Minimalistic functions set to encode and encode CBOR data (RFC7049)
 * Main idea and functions taken from cbor-java library.
 */
object PicoCbor {

    /**
     * Major CBOR types
     */
    enum class MajorType(val value: Int) {
        INVALID(-1),
        UNSIGNED_INTEGER(0),
        NEGATIVE_INTEGER(1),
        BYTE_STRING(2),
        UNICODE_STRING(3),
        ARRAY(4),
        MAP(5),
        TAG(6),
        SPECIAL(7);

        companion object {
            fun ofByte(b: Int): MajorType {
                return when ((b and 0xFF) shr 5) {
                    UNSIGNED_INTEGER.value -> UNSIGNED_INTEGER
                    NEGATIVE_INTEGER.value -> NEGATIVE_INTEGER
                    BYTE_STRING.value -> BYTE_STRING
                    UNICODE_STRING.value -> UNICODE_STRING
                    ARRAY.value -> ARRAY
                    MAP.value -> MAP
                    TAG.value -> TAG
                    SPECIAL.value -> SPECIAL
                    else -> INVALID
                }
            }
        }
    }

    /**
     * Additional flags to encode data size/length
     */
    enum class AdditionalInformation(val value: Int) {
        DIRECT(0),          // 0-23, direct value in the one byte
        ONE_BYTE(24),       // 24
        TWO_BYTES(25),      // 25
        FOUR_BYTES(26),     // 26
        EIGHT_BYTES(27),    // 27
        RESERVED(28),       // 28-30
        INDEFINITE(31);

        companion object {
            fun ofByte(b: Int): AdditionalInformation {
                return when (b and 31) {
                    ONE_BYTE.value -> ONE_BYTE
                    TWO_BYTES.value -> TWO_BYTES
                    FOUR_BYTES.value -> FOUR_BYTES
                    EIGHT_BYTES.value -> EIGHT_BYTES
                    28, 29, 30 -> RESERVED
                    INDEFINITE.value -> INDEFINITE
                    else -> DIRECT
                }
            }
        }
    }

    /**
     * Subtypes for special data types
     */
    enum class SpecialType {
        SIMPLE_VALUE, SIMPLE_VALUE_NEXT_BYTE,
        IEEE_754_HALF_PRECISION_FLOAT, IEEE_754_SINGLE_PRECISION_FLOAT, IEEE_754_DOUBLE_PRECISION_FLOAT,
        UNALLOCATED, BREAK;

        companion object {
            fun ofByte(b: Int): SpecialType {
                return when (b and 31) {
                    24 -> SIMPLE_VALUE_NEXT_BYTE
                    25 -> IEEE_754_HALF_PRECISION_FLOAT
                    26 -> IEEE_754_SINGLE_PRECISION_FLOAT
                    27 -> IEEE_754_DOUBLE_PRECISION_FLOAT
                    28, 29, 30 -> UNALLOCATED
                    31 -> BREAK
                    else -> SIMPLE_VALUE
                }
            }
        }
    }

    /*
     * ==== Encoders ====
     *
     * NOTE: All encoders return ByteArray with encoded data
     */

    /**
     * Encode integer value
     */
    fun encodeInt(value: Int): ByteArray {
        return encodeLong(value.toLong())
    }

    fun encodeLong(value: Long): ByteArray {
        return if(value >= 0) encodeTypeAndLong(MajorType.UNSIGNED_INTEGER, value)
        else encodeTypeAndLong(MajorType.NEGATIVE_INTEGER, (-1L - value).absoluteValue)
    }

    private fun encodeTypeAndLong(majorType: MajorType, value: Long) : ByteArray {
        val symbol = majorType.value shl 5

        return when {
            value <= 23L -> {
                byteArrayOf((symbol or value.toInt()).toByte())
            }
            value <= 255L -> {
                byteArrayOf((symbol or AdditionalInformation.ONE_BYTE.value).toByte(), value.toByte())
            }
            value <= 65535L -> {
                byteArrayOf((symbol or AdditionalInformation.TWO_BYTES.value).toByte(), (value shr 8 and 0xFF).toByte(), (value and 0xFF).toByte())
            }
            value <= 4294967295L -> {
                byteArrayOf((symbol or AdditionalInformation.FOUR_BYTES.value).toByte(),
                    (value shr 24 and 0xFF).toByte(), (value shr 16 and 0xFF).toByte(),
                    (value shr 8 and 0xFF).toByte(), (value and 0xFF).toByte()
                )
            }
            else -> {
                byteArrayOf((symbol or AdditionalInformation.EIGHT_BYTES.value).toByte(),
                    (value shr 56 and 0xFF).toByte(), (value shr 48 and 0xFF).toByte(),
                    (value shr 40 and 0xFF).toByte(), (value shr 32 and 0xFF).toByte(),
                    (value shr 24 and 0xFF).toByte(), (value shr 16 and 0xFF).toByte(),
                    (value shr 8 and 0xFF).toByte(), (value and 0xFF).toByte())
            }
        }
    }

    /**
     * Encode boolean true|false
     */
    fun encodeBoolean(value: Boolean): ByteArray {
        return byteArrayOf((if(value) (7 shl 5 or 21) else (7 shl 5 or 20)).toByte())
    }

    /**
     * Encode Float in SingleFloat representation only, always 4 bytes long.
     */
    fun encodeFloat(value: Float) : ByteArray {
        val bits = value.toRawBits()
        return byteArrayOf((7 shl 5 or 26).toByte(),
            (bits shr 24 and 0xFF).toByte(), (bits shr 16 and 0xFF).toByte(),
            (bits shr 8 and 0xFF).toByte(), (bits shr 0 and 0xFF).toByte()
        )
    }

    /**
     * Encode fixed size byte array
     */
    fun encodeByteString(bytes: ByteArray) : ByteArray {
        return encodeTypeAndLong(MajorType.BYTE_STRING, bytes.size.toLong()) + bytes
    }

    /**
     * Encode fixed length string in UTF-8
     */
    fun encodeString(value: String) : ByteArray {
        val bytes = value.encodeToByteArray()
        return encodeTypeAndLong(MajorType.UNICODE_STRING, bytes.size.toLong()) + bytes
    }

    /**
     * Encode fixed size array's header.
     * Array's data should be encoded separately after header.
     */
    fun encodeArray(size: Int): ByteArray {
        return encodeTypeAndLong(MajorType.ARRAY, size.toLong())
    }

    /**
     * Encode fixed size map's header.
     */
    fun encodeMap(size: Int): ByteArray {
        return encodeTypeAndLong(MajorType.MAP, size.toLong())
    }

}
