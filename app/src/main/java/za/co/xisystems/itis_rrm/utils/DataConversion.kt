package za.co.xisystems.itis_rrm.utils

import java.util.*

/**
 * Created by Pieter Jacobs on 2016/02/02.
 * Updated by Pieter Jacobs during 2016/04, 2016/07.
 */
object DataConversion {
    // region (Public Static Methods)
// Converts a DB Hex GUID to a guid required in C#
    fun toLittleEndian(bigEndian: String?): String? { //        if (isStub())
//            return bigEndian;
//        else {
        if (null == bigEndian || bigEndian.length % 2 != 0) return null
        val b = bigEndianHexStringToByteArray(bigEndian)
        return toLittleEndian(b)
        //        }
    }

    // Converts a guid used in C# to a DB Hex GUID
    fun toBigEndian(littleEndian: String?): String? { //        if (isStub())
//            return littleEndian;
//        else {
        if (null == littleEndian || littleEndian.length % 2 != 0) return null
        val l = littleEndianToByteArray(littleEndian)
        return toBigEndian(l)
        //        }
    }

    // Already correctly sorted byte[] to DB Hex Guid
    fun bigEndianToString(bigEndian: ByteArray): String {
        val stringBuilder = StringBuilder()
        for (b in bigEndian) {
            stringBuilder.append(String.format("%02x", b))
        }
        return stringBuilder.toString().toUpperCase(Locale.ROOT)
    }

    // Actually one of Mauritz' methods.  I've moved it here because I need to make sure no
// conversion is required where the method is/was used.
// The parameter should be a DB Hex GUID (Big Endian).
// There should be NO dashes to start off with!
    fun removeDashesAndUppercaseString(shouldBeABigEndianGuid: String?): String? {
        return shouldBeABigEndianGuid?.toUpperCase(Locale.ROOT)?.replace("-", "")
    }

    // endregion (Public Static Methods)
// region (Private Static Methods)
// Swap the elements in the byte[] between bigEndian and littleEndian formats
    private fun swapBytes(byteArray: ByteArray): ByteArray {
        val tmpByteArray = byteArray.clone()
        tmpByteArray[0] = byteArray[3]
        tmpByteArray[1] = byteArray[2]
        tmpByteArray[2] = byteArray[1]
        tmpByteArray[3] = byteArray[0]
        tmpByteArray[4] = byteArray[5]
        tmpByteArray[5] = byteArray[4]
        tmpByteArray[6] = byteArray[7]
        tmpByteArray[7] = byteArray[6]
        return tmpByteArray
    }

    // Convert the GUID Hex String to a byte[]
    private fun bigEndianHexStringToByteArray(bigEndian: String): ByteArray {
        val stringLength = bigEndian.length
        val bytes = ByteArray(stringLength / 2)
        var i = 0
        while (i < stringLength) {
            bytes[i / 2] = ((Character.digit(
                bigEndian[i],
                16
            ) shl 4) + Character.digit(bigEndian[i + 1], 16)).toByte()
            i += 2
        }
        return bytes
    }

    // Converts a DB Hex GUID (as byte[]) to a guid required in C#
    private fun toLittleEndian(bigEndian: ByteArray): String {
        val littleEndian = swapBytes(bigEndian)
        val stringBuilder = StringBuilder()
        for ((i, b) in littleEndian.withIndex()) {
            if (listOf(4, 6, 8, 10).contains(i)) stringBuilder.append("-")
            stringBuilder.append(String.format("%02x", b))
        }
        return stringBuilder.toString().toLowerCase(Locale.ROOT)
    }

    private fun littleEndianToByteArray(littleEndian: String): ByteArray {
        val tmpString = littleEndian.replace("([{}\\-])".toRegex(), "")
        val stringLength = tmpString.length
        val bytes = ByteArray(stringLength / 2)
        var i = 0
        while (i < stringLength) {
            bytes[i / 2] = ((Character.digit(
                tmpString[i],
                16
            ) shl 4) + Character.digit(tmpString[i + 1], 16)).toByte()
            i += 2
        }
        return bytes
    }

    // Converts a guid used in C# (as byte[]) to a DB Hex GUID
    private fun toBigEndian(littleEndian: ByteArray): String {
        val bigEndian = swapBytes(littleEndian)
        val stringBuilder = StringBuilder()
        for (b in bigEndian) {
            stringBuilder.append(String.format("%02x", b))
        }
        return stringBuilder.toString().toUpperCase(Locale.ROOT)
    } // endregion (Private Static Methods)
}
