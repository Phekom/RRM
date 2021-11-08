/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.utils

import com.password4j.SecureString
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.util.Arrays
import kotlin.math.round
import okhttp3.internal.and
import timber.log.Timber

@Suppress("MagicNumber")
object Utils {

    fun insertDashUUID(uuid: String?): String {
        var sb = StringBuffer(uuid!!)
        sb.insert(8, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(13, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(18, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(23, "-")
        return sb.toString()
    }

    fun nanCheck(toString: String): Boolean {
        return try {
            val dbl = toString.toDouble()
            dbl.isNaN()
        } catch (e: Exception) {
            Timber.e(e, "Double conversion error: ${e.message}")
            true
        }
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8

    private val TO_BASE64 = charArrayOf(
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
        'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
        'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+',
        '/'
    )

    private val FROM_BASE64 = IntArray(256)

    fun setup() {
        Arrays.fill(FROM_BASE64, -1)
        for (i in TO_BASE64.indices) {
            FROM_BASE64[TO_BASE64[i].code] = i
        }
        FROM_BASE64['='.code] = -2
    }

    fun fromCharSequenceToBytes(charSequence: CharSequence?): ByteArray {
        if (charSequence == null) {
            return ByteArray(0)
        }
        val encoder = DEFAULT_CHARSET.newEncoder()
        val length = charSequence.length
        val arraySize = scale(length, encoder.maxBytesPerChar())
        val result = ByteArray(arraySize)
        return if (length == 0) {
            result
        } else {
            var charArray: CharArray?
            charArray = if (charSequence is String) {
                charSequence.toCharArray()
            } else {
                fromCharSequenceToChars(charSequence)
            }
            charArray = Arrays.copyOfRange(charArray, 0, length)
            encoder.onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE).reset()
            val byteBuffer = ByteBuffer.wrap(result)
            val charBuffer = CharBuffer.wrap(charArray, 0, length)
            encoder.encode(charBuffer, byteBuffer, true)
            encoder.flush(byteBuffer)
            result.copyOf(byteBuffer.position())
        }
    }

    fun fromCharSequenceToChars(charSequence: CharSequence?): CharArray {
        if (charSequence == null || charSequence.isEmpty()) {
            return CharArray(0)
        }
        val result = CharArray(charSequence.length)
        for (i in charSequence.indices) {
            result[i] = charSequence[i]
        }
        return result
    }

    fun append(cs1: CharSequence?, cs2: CharSequence?): CharSequence? {
        return when {
            cs1.isNullOrEmpty() -> {
                cs2
            }
            cs2.isNullOrEmpty() -> {
                cs1
            }
            else -> {
                val charArray1 = fromCharSequenceToChars(cs1)
                val charArray2 = fromCharSequenceToChars(cs2)
                val result = CharArray(charArray1.size + charArray2.size)
                System.arraycopy(charArray1, 0, result, 0, charArray1.size)
                System.arraycopy(charArray2, 0, result, charArray1.size, charArray2.size)
                SecureString(result)
            }
        }
    }

    fun littleEndianToLong(bs: ByteArray, off: Int): Long {
        val lo = littleEndianToInt(bs, off)
        val hi = littleEndianToInt(bs, off + 4)
        return (hi and 0xffffffffL.toInt() shl 32 or (lo and 0xffffffffL.toInt())).toLong()
    }

    fun littleEndianToInt(bs: ByteArray, off: Int): Int {
        var offset = off
        var n: Int = bs[offset] and 0xff
        n = n or (bs[++offset] and 0xff shl 8)
        n = n or (bs[++offset] and 0xff shl 16)
        n = n or (bs[++offset].toInt() shl 24)
        return n
    }

    fun intToLittleEndianBytes(a: Int): ByteArray {
        val result = ByteArray(4)
        result[0] = (a and 0xFF).toByte()
        result[1] = (a shr 8 and 0xFF).toByte()
        result[2] = (a shr 16 and 0xFF).toByte()
        result[3] = (a shr 24 and 0xFF).toByte()
        return result
    }

    fun littleEndianBytesToLong(b: ByteArray): Long {
        var result: Long = 0
        for (i in 7 downTo 0) {
            result = result shl 8
            result = result or ((b[i] and 0xFF).toLong())
        }
        return result
    }

    fun intToLong(x: Int): Long {
        val intBytes = intToLittleEndianBytes(x)
        val bytes = ByteArray(8)
        System.arraycopy(intBytes, 0, bytes, 0, 4)
        return littleEndianBytesToLong(bytes)
    }

    private fun scale(initialLength: Int, bytesPerChar: Float): Int {
        return (initialLength.toDouble() * bytesPerChar.toDouble()).toInt()
    }

    fun encodeBase64(src: ByteArray): String {
        return encodeBase64(src, true)
    }

    fun encodeBase64(src: ByteArray, padding: Boolean): String {
        val encoded = encode(src, padding)
        return String(encoded, 0, encoded.size)
    }

    fun encode(src: ByteArray, padding: Boolean): ByteArray {
        val len = outLength(src.size, padding)
        val dst = ByteArray(len)
        val ret = encode(src, src.size, dst, padding)
        return if (ret != dst.size) {
            dst.copyOf(ret)
        } else dst
    }

    private fun outLength(length: Int, doPadding: Boolean): Int {
        return if (doPadding) {
            4 * ((length + 2) / 3)
        } else {
            val n = length % 3
            4 * (length / 3) + if (n == 0) 0 else n + 1
        }
    }

    private fun encode(src: ByteArray, end: Int, dst: ByteArray, padding: Boolean): Int {
        val base64 = TO_BASE64
        var sp = 0
        val length = end / 3 * 3
        var dp = 0
        while (sp < length) {
            val sl0 = sp + length
            var sp0 = sp
            var dp0 = dp
            while (sp0 < sl0) {
                val bits: Int = src[sp0] and 0xff shl 16 or (src[sp0 + 1] and 0xff shl 8) or (src[sp0 + 2] and 0xff)
                dst[dp0] = base64[bits ushr 18 and 0x3f].code.toByte()
                dst[dp0 + 1] = base64[bits ushr 12 and 0x3f].code.toByte()
                dst[dp0 + 2] = base64[bits ushr 6 and 0x3f].code.toByte()
                dst[dp0 + 3] = base64[bits and 0x3f].code.toByte()
                sp0 += 3
                dp0 += 4
            }
            val dlen = (sl0 - sp) / 3 * 4
            dp += dlen
            sp = sl0
        }
        if (sp < end) {
            val b0: Int = src[sp++] and 0xff
            dst[dp++] = base64[b0 shr 2].code.toByte()
            if (sp == end) {
                dst[dp++] = base64[b0 shl 4 and 0x3f].code.toByte()
                if (padding) {
                    dst[dp++] = '='.code.toByte()
                    dst[dp++] = '='.code.toByte()
                }
            } else {
                val b1: Int = src[sp] and 0xff
                dst[dp++] = base64[b0 shl 4 and 0x3f or (b1 shr 4)].code.toByte()
                dst[dp++] = base64[b1 shl 2 and 0x3f].code.toByte()
                if (padding) {
                    dst[dp++] = '='.code.toByte()
                }
            }
        }
        return dp
    }
}
