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

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * CBOR decoder tests
 */
@OptIn(ExperimentalStdlibApi::class)
class PicoCborDecoderTest {

    @Test
    fun decodeOneInt64() {
        val x = 64

        val cborX = PicoCbor.encodeInt(x)
        assertEquals("1840", cborX.toHexString())

        val decoder = PicoCborDecoder(cborX)
        assertEquals(x, decoder.int())
    }

    @Test
    fun decodeOneInt128() {
        val y = 128

        val cborY = PicoCbor.encodeInt(y)
        assertEquals("1880", cborY.toHexString())

        val decoder = PicoCborDecoder(cborY)
        assertEquals(y, decoder.int())
    }

    @Test
    fun decodeTwoInts() {
        val x = 64
        val y = 128

        val cborX = PicoCbor.encodeInt(x)
        assertEquals("1840", cborX.toHexString())
        val cborY = PicoCbor.encodeInt(y)
        assertEquals("1880", cborY.toHexString())

        val decoder = PicoCborDecoder(cborX + cborY)
        assertEquals(x, decoder.int())
        assertEquals(y, decoder.int())
    }

    @Test
    fun decodeLong() {
        // test cases taken from encodeLong() tests
        val encoded = "00" + // 0L
                "01" +       // 1L
                "17" +       // 23L
                "1818" +     // 24L
                "18ff" +     // 255L
                "19ffff" +   // 65535L
                "1a00010000" + // 65536L
                "1b000000e8d4a51000" +  // 1000000000000L
                "1b7fffffffffffffff" +  // 9223372036854775807L == Long.MAX_VALUE
                "3b7fffffffffffffff"    // -9223372036854775807L - 1L == Long.MIN_VALUE

        val decoder = PicoCborDecoder(encoded.hexToByteArray())
        assertEquals(0L, decoder.long())
        assertEquals(1L, decoder.long())
        assertEquals(23L, decoder.long())
        assertEquals(24L, decoder.long())
        assertEquals(255L, decoder.long())
        assertEquals(65535L, decoder.long())
        assertEquals(65536L, decoder.long())
        assertEquals(1000000000000L, decoder.long())
        assertEquals(9223372036854775807L, decoder.long())
        assertEquals(-9223372036854775807L - 1L, decoder.long())
    }

    @Test
    fun decodeFloats() {
        val src = "fa47c35000" +  // 100000.0F
                "fa7f7fffff" + // 3.4028234663852886e+38F
                "fa00000000" + // 0.0F
                "fa3f800000" + // 1.0F
                "fa3fc00000" + // 1.5F
                "fa477fe000" + // 65504.0F
                "fabf800000" + // -1.0F
                "fac7c35000"   // -100000.0F

        val decoder = PicoCborDecoder(src.hexToByteArray())

        assertEquals(100000.0F, decoder.float())
        assertEquals(3.4028234663852886e+38F, decoder.float())
        assertEquals(0.0F, decoder.float())
        assertEquals(1.0F, decoder.float())
        assertEquals(1.5F, decoder.float())
        assertEquals(65504.0F, decoder.float())
        assertEquals(-1.0F, decoder.float())
        assertEquals(-100000.0F, decoder.float())
    }

    @Test
    fun decodeByteString() {
        val src = "40" +
                "4161" +
                "4449455446" +
                "5818000102030405060708090a0b0c0d0e0f1011121314151617"

        val decoder = PicoCborDecoder(src.hexToByteArray())

        assertEquals("", decoder.byteString().toHexString())
        assertEquals("61", decoder.byteString().toHexString())
        assertEquals("49455446", decoder.byteString().toHexString())
        assertEquals("000102030405060708090a0b0c0d0e0f1011121314151617", decoder.byteString().toHexString())
    }


    @Test
    fun decodeStrings() {
        val src = "60" +  // ""
                "6161" +              // a
                "6449455446" +        // IETF
                "62225c" +            // "\
                "62c3bc" +            // ü
                "6ad0b0d0b1d0b2d0b3d0b4"  // абвгд

        val decoder = PicoCborDecoder(src.hexToByteArray())

        assertEquals("", decoder.string())
        assertEquals("a", decoder.string())
        assertEquals("IETF", decoder.string())
        assertEquals("\"\\", decoder.string())
        assertEquals("ü", decoder.string())
        assertEquals("абвгд", decoder.string())
    }


    @Test
    fun decodeArrays() {
        val src = "80" +  // empty array
                "8100" +  // [0]
                "976060606060606060606060606060606060606060606060" +    // 23 empty strings
                "9818010101010101010101010101010101010101010101010101"  // 24 ones

        val decoder = PicoCborDecoder(src.hexToByteArray())
        assertEquals(0, decoder.arraySize())

        assertEquals(1, decoder.arraySize())
        assertEquals(0, decoder.int())

        assertEquals(23, decoder.arraySize())
        repeat(23) {
            assertEquals("", decoder.string())
        }

        assertEquals(24, decoder.arraySize())
        repeat(24) {
            assertEquals(1, decoder.int())
        }

    }

    @Test
    fun decodeFloatArrays() {
        val src = "80" +         // empty array
                "81fa40800000" + // 4F
                "82fa40800000fa3f800000" + // 4F, 1F
                "83fa40800000fa3f800000fa40000000" // 4F, 1F, 2F

        val decoder = PicoCborDecoder(src.hexToByteArray())

        assertEquals(0, decoder.floatArray().size)

        val one = decoder.floatArray()
        assertEquals(1, one.size)
        assertEquals(4F, one[0])

        val two = decoder.floatArray()
        assertEquals(2, two.size)
        assertEquals(4F, two[0])
        assertEquals(1F, two[1])

        val three = decoder.floatArray()
        assertEquals(3, three.size)
        assertEquals(4F, three[0])
        assertEquals(1F, three[1])
        assertEquals(2F, three[2])
    }

    @Test
    fun decodeMaps() {
        val src = "a0" +            // empty Map
                "a10060" +          // <0,"">
                "a2616101616202"    // <a,1>, <b,2>

        val decoder = PicoCborDecoder(src.hexToByteArray())

        assertEquals(0, decoder.mapSize())

        assertEquals(1, decoder.mapSize())
        assertEquals(0, decoder.int())
        assertEquals("", decoder.string())

        assertEquals(2, decoder.mapSize())
        assertEquals("a", decoder.string())
        assertEquals(1, decoder.int())
        assertEquals("b", decoder.string())
        assertEquals(2, decoder.int())
    }

}
