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
 * CBOR encoders tests
 *
 * Test data taken from here: https://github.com/cbor/test-vectors/blob/master/appendix_a.json
 */
class CborEncoderTest {

    @Test
    fun encodeInt() {
        assertEquals("00", PicoCbor.encodeInt(0).toHexString())
        assertEquals("01", PicoCbor.encodeInt(1).toHexString())
        assertEquals("0a", PicoCbor.encodeInt(10).toHexString())
        assertEquals("17", PicoCbor.encodeInt(23).toHexString())    // one byte integer edge
        assertEquals("1818", PicoCbor.encodeInt(24).toHexString())
        assertEquals("1819", PicoCbor.encodeInt(25).toHexString())
        assertEquals("18ff", PicoCbor.encodeInt(255).toHexString())

        assertEquals("1903e8", PicoCbor.encodeInt(1000).toHexString())
        assertEquals("1a000f4240", PicoCbor.encodeInt(1000000).toHexString())

        // negative
        assertEquals("20", PicoCbor.encodeInt(-1).toHexString())
        assertEquals("29", PicoCbor.encodeInt(-10).toHexString())
        assertEquals("3863", PicoCbor.encodeInt(-100).toHexString())
        assertEquals("3903e7", PicoCbor.encodeInt(-1000).toHexString())
        assertEquals("3a000f423f", PicoCbor.encodeInt(-1000000).toHexString())
    }

    @Test
    fun encodeLong() {
        assertEquals("00", PicoCbor.encodeLong(0L).toHexString())
        assertEquals("01", PicoCbor.encodeLong(1L).toHexString())
        assertEquals("17", PicoCbor.encodeLong(23L).toHexString())   // one byte integer edge
        assertEquals("1818", PicoCbor.encodeLong(24L).toHexString())
        assertEquals("18ff", PicoCbor.encodeLong(255L).toHexString())
        assertEquals("19ffff", PicoCbor.encodeLong(65535L).toHexString())
        assertEquals("1a00010000", PicoCbor.encodeLong(65536L).toHexString())
        assertEquals("1b000000e8d4a51000", PicoCbor.encodeLong(1000000000000L).toHexString())
    }

    @Test
    fun encodeBoolean() {
        assertEquals("f4", PicoCbor.encodeBoolean(false).toHexString())
        assertEquals("f5", PicoCbor.encodeBoolean(true).toHexString())
    }

        @Test
    fun encodeFloat() {
        //NOTE: we use only SingleFloat
        assertEquals("fa47c35000", PicoCbor.encodeFloat(100000.0F).toHexString())
        assertEquals("fa7f7fffff", PicoCbor.encodeFloat(3.4028234663852886e+38F).toHexString())

        // missed in test vectors, taken from encoding results
        assertEquals("fa00000000", PicoCbor.encodeFloat(0.0F).toHexString())
        assertEquals("fa3f800000", PicoCbor.encodeFloat(1.0F).toHexString())
        assertEquals("fa3fc00000", PicoCbor.encodeFloat(1.5F).toHexString())
        assertEquals("fa477fe000", PicoCbor.encodeFloat(65504.0F).toHexString())
        assertEquals("fabf800000", PicoCbor.encodeFloat(-1.0F).toHexString())
        assertEquals("fac7c35000", PicoCbor.encodeFloat(-100000.0F).toHexString())

        // some Float sizes used in BSVG tests
        assertEquals("fa40000000", PicoCbor.encodeFloat(2F).toHexString())
        assertEquals("fa40400000", PicoCbor.encodeFloat(3F).toHexString())
        assertEquals("fa40800000", PicoCbor.encodeFloat(4F).toHexString())
        assertEquals("fa40c00000", PicoCbor.encodeFloat(6F).toHexString())
        assertEquals("fa41000000", PicoCbor.encodeFloat(8F).toHexString())
        assertEquals("fa41800000", PicoCbor.encodeFloat(16F).toHexString())
        assertEquals("fa41c00000", PicoCbor.encodeFloat(24F).toHexString())
        assertEquals("fa42000000", PicoCbor.encodeFloat(32F).toHexString())
    }

    @Test
    fun encodeByteString() {
        assertEquals("40", PicoCbor.encodeByteString(ByteArray(0)).toHexString())
        assertEquals("4161", PicoCbor.encodeByteString(byteArrayOf('a'.code.toByte())).toHexString())
        assertEquals("4449455446", PicoCbor.encodeByteString(byteArrayOf(0x49, 0x45, 0x54, 0x46)).toHexString())
        assertEquals("5818000102030405060708090a0b0c0d0e0f1011121314151617",
            PicoCbor.encodeByteString( ByteArray(24) { it.toByte() }).toHexString())
    }

    @Test
    fun encodeString() {
        assertEquals("60", PicoCbor.encodeString("").toHexString())
        assertEquals("6161", PicoCbor.encodeString("a").toHexString())
        assertEquals("6449455446", PicoCbor.encodeString("IETF").toHexString())
        assertEquals("62225c", PicoCbor.encodeString("\"\\").toHexString())
        assertEquals("62c3bc", PicoCbor.encodeString("ü").toHexString())

        assertEquals("6ad0b0d0b1d0b2d0b3d0b4", PicoCbor.encodeString("абвгд").toHexString())
    }

    @Test
    fun encodeArray() {
        assertEquals("80", PicoCbor.encodeArray(0).toHexString())
        assertEquals("81", PicoCbor.encodeArray(1).toHexString())
        assertEquals("8a", PicoCbor.encodeArray(10).toHexString())
        assertEquals("97", PicoCbor.encodeArray(23).toHexString())
        assertEquals("9818", PicoCbor.encodeArray(24).toHexString())
        assertEquals("9819", PicoCbor.encodeArray(25).toHexString())
        assertEquals("98ff", PicoCbor.encodeArray(255).toHexString())
    }

    @Test
    fun encodeMap() {
        assertEquals("a0", PicoCbor.encodeMap(0).toHexString())
        assertEquals("a2", PicoCbor.encodeMap(2).toHexString())
    }

}
