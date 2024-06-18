/*
 * (c) VAP Communications Group, 2021
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

/**
 * CBOR data decoder from a single ByteArray.
 * Parsing of source array similar to reading of stream starting from given offset.
 *
 * NOTE: decoders may throw PicoCborException on malformed source data.
 */
class PicoCborDecoder(private val src: ByteArray, private var offset: Int = 0) {

    /**
     * Decode one Int value
     */
    fun int(): Int {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF

        return when (PicoCbor.MajorType.ofByte(b)) {
            PicoCbor.MajorType.UNSIGNED_INTEGER -> unsignedInt()
            PicoCbor.MajorType.NEGATIVE_INTEGER -> {
                -1 - unsignedInt()
            }

            else -> throw PicoCborException(111, 1, "$offset, $b")
        }
    }

    /**
     * Decode unsigned integer, but returns signed Int
     */
    private fun unsignedInt(): Int {
        val b = src[offset].toInt() and 0xFF

        when (PicoCbor.AdditionalInformation.ofByte(b)) {
            PicoCbor.AdditionalInformation.DIRECT -> {
                offset += 1
                return b and 31
            }

            PicoCbor.AdditionalInformation.ONE_BYTE -> {
                checkSize(2)
                val result = src[offset + 1].toInt() and 0xFF
                offset += 2
                return result
            }

            PicoCbor.AdditionalInformation.TWO_BYTES -> {
                checkSize(3)
                val hb = (src[offset + 1].toInt() shl 8) and 0xFF00
                val lb = src[offset + 2].toInt() and 0xFF
                val result = hb or lb
                offset += 3
                return result
            }

            PicoCbor.AdditionalInformation.FOUR_BYTES -> {
                checkSize(5)
                val fourByteValue: Int =
                    ((src[offset + 1].toInt() and 0xFF) shl 24) or
                    ((src[offset + 2].toInt() and 0xFF) shl 16) or
                    ((src[offset + 3].toInt() and 0xFF) shl 8) or
                     (src[offset + 4].toInt() and 0xFF)

                offset += 5
                return fourByteValue
            }

            else -> throw PicoCborException(112, 1, "$offset, $b")
        }
    }

    /**
     * Decode one Long value
     */
    fun long(): Long {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF

        return when (PicoCbor.MajorType.ofByte(b)) {
            PicoCbor.MajorType.UNSIGNED_INTEGER -> unsignedLong()
            PicoCbor.MajorType.NEGATIVE_INTEGER -> {
                -1 - unsignedLong()
            }

            else -> throw PicoCborException(119, 1, "$offset, $b")
        }
    }

    /**
     * Decode unsigned integer, but returns signed Long
     */
    private fun unsignedLong(): Long {
        val b = src[offset].toInt() and 0xFF

        when (PicoCbor.AdditionalInformation.ofByte(b)) {
            PicoCbor.AdditionalInformation.DIRECT -> {
                offset += 1
                return b.toLong() and 31L
            }

            PicoCbor.AdditionalInformation.ONE_BYTE -> {
                checkSize(2)
                val result = src[offset + 1].toInt() and 0xFF
                offset += 2
                return result.toLong()
            }

            PicoCbor.AdditionalInformation.TWO_BYTES -> {
                checkSize(3)
                val hb = (src[offset + 1].toInt() shl 8) and 0xFF00
                val lb = src[offset + 2].toInt() and 0xFF
                val result = hb or lb
                offset += 3
                return result.toLong()
            }

            PicoCbor.AdditionalInformation.FOUR_BYTES -> {
                checkSize(5)
                val fourByteValue: Int =
                    ((src[offset + 1].toInt() and 0xFF) shl 24) or
                    ((src[offset + 2].toInt() and 0xFF) shl 16) or
                    ((src[offset + 3].toInt() and 0xFF) shl 8) or
                    (src[offset + 4].toInt() and 0xFF)

                offset += 5
                return fourByteValue.toLong()
            }

            PicoCbor.AdditionalInformation.EIGHT_BYTES -> {
                checkSize(9)
                val eightByteValue: Long =
                    ((src[offset + 1].toLong() and 0xFF) shl 56) or
                    ((src[offset + 2].toLong() and 0xFF) shl 48) or
                    ((src[offset + 3].toLong() and 0xFF) shl 40) or
                    ((src[offset + 4].toLong() and 0xFF) shl 32) or
                    ((src[offset + 5].toLong() and 0xFF) shl 24) or
                    ((src[offset + 6].toLong() and 0xFF) shl 16) or
                    ((src[offset + 7].toLong() and 0xFF) shl 8) or
                    (src[offset + 8].toLong() and 0xFF)

                offset += 9
                return eightByteValue
            }

            else -> throw PicoCborException(120, 1, "$offset, $b")
        }
    }


    /**
     * Decode Float value in IEEE_754_SINGLE_PRECISION_FLOAT
     */
    fun float(): Float {
        checkSize(5)

        val b = src[offset].toInt() and 0xFF

        if (PicoCbor.SpecialType.ofByte(b) == PicoCbor.SpecialType.IEEE_754_SINGLE_PRECISION_FLOAT) {
            val intValue =
                ((src[offset + 1].toInt() and 0xFF) shl 24) or
                ((src[offset + 2].toInt() and 0xFF) shl 16) or
                ((src[offset + 3].toInt() and 0xFF) shl 8) or
                 (src[offset + 4].toInt() and 0xFF)

            offset += 5
            return Float.fromBits(intValue)
        } else throw PicoCborException(113, 1, "$offset, $b")
    }

    /**
     * Decode fixed size byte string to ByteArray
     */
    fun byteString(): ByteArray {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF

        if (PicoCbor.MajorType.ofByte(b) == PicoCbor.MajorType.BYTE_STRING) {
            val size = unsignedInt()
            return if (size == 0)
                ByteArray(0)
            else {
                checkSize(size)
                val result = src.copyOfRange(offset, offset + size)
                offset += size
                return result
            }
        } else throw PicoCborException(114, 1, "$offset, $b")
    }

    /**
     * Decode UTF-8 fixed length string
     */
    fun string(): String {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF
        if (PicoCbor.MajorType.ofByte(b) == PicoCbor.MajorType.UNICODE_STRING) {
            val size = unsignedInt() // string's size in bytes
            return if (size == 0) ""
            else {
                checkSize(size)
                val result = src.decodeToString(offset, offset + size)
                offset += size
                return result
            }
        } else throw PicoCborException(115, 1, "$offset, $b")
    }

    /**
     * Decode header of fixed size array.
     * @return number of elements in array, all data should be decoded separately.
     */
    fun arraySize(): Int {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF

        return when (PicoCbor.MajorType.ofByte(b)) {
            PicoCbor.MajorType.ARRAY -> unsignedInt()
            else -> throw PicoCborException(116, 1, "$offset, $b")
        }
    }

    /**
     * Decode fixed size array of Float elements.
     * Array may be empty, in this case only header is decoded.
     */
    fun floatArray(): FloatArray {
        val floats = arraySize()
        val fa = FloatArray(floats)

        repeat(floats) { index ->
            fa[index] = float()
        }

        return fa
    }

    /**
     * Decode header of fixed size map.
     * @return number of elements in map
     */
    fun mapSize(): Int {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF

        return when (PicoCbor.MajorType.ofByte(b)) {
            PicoCbor.MajorType.MAP -> unsignedInt()
            else -> throw PicoCborException(117, 1, "$offset, $b")
        }
    }

    /**
     * Decode single Boolean falue
     */
    fun boolean(): Boolean {
        checkSize(1)
        val b = src[offset].toInt() and 0xFF

        return when (b) {
            0xf4 -> { offset++; false}
            0xf5 -> { offset++; true}
            else -> throw PicoCborException(118, 1, "$offset, $b")
        }
    }

    /**
     * Check available data to decode in the source byte array
     */
    private fun checkSize(size: Int) {
        if (offset + size > src.size)
            throw PicoCborException(110, 1, "${src.size}, $offset, $size")
    }

}
