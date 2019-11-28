package za.co.xisystems.itis_rrm.utils;

import java.util.Arrays;


/**
 * Created by Pieter Jacobs on 2016/02/02.
 * Updated by Pieter Jacobs during 2016/04, 2016/07.
 */
public class DataConversion {

    // region (Public Static Methods)

    // Converts a DB Hex GUID to a guid required in C#
    public static String toLittleEndian(String bigEndian) {
//        if (isStub())
//            return bigEndian;
//        else {
            if ((null == bigEndian) || (!(bigEndian.length() % 2 == 0)))
                return null;
            byte[] b = bigEndianHexStringToByteArray(bigEndian);
            return toLittleEndian(b);
//        }
    }

    // Converts a guid used in C# to a DB Hex GUID
    public static String toBigEndian(String littleEndian) {
//        if (isStub())
//            return littleEndian;
//        else {
            if ((null == littleEndian) || (!(littleEndian.length() % 2 == 0)))
                return null;
            byte[] l = littleEndianToByteArray(littleEndian);
            return toBigEndian(l);
//        }
    }

    // Already correctly sorted byte[] to DB Hex Guid
    public static String bigEndianToString(byte[] bigEndian) {
        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : bigEndian) {
            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString().toUpperCase();
    }

    // Actually one of Mauritz' methods.  I've moved it here because I need to make sure no
    // conversion is required where the method is/was used.
    // The parameter should be a DB Hex GUID (Big Endian).
    // There should be NO dashes to start off with!
    public static String removeDashesAndUppercaseString(String shouldBeABigEndianGuid) {
        if (null == shouldBeABigEndianGuid) return null;
        return shouldBeABigEndianGuid.toUpperCase().replace("-", "");
    }

    // endregion (Public Static Methods)

    // region (Private Static Methods)

    // Swap the elements in the byte[] between bigEndian and littleEndian formats
    private static byte[] swapBytes(byte[] byteArray) {
        byte[] tmpByteArray = byteArray.clone();
        tmpByteArray[0] = byteArray[3];
        tmpByteArray[1] = byteArray[2];
        tmpByteArray[2] = byteArray[1];
        tmpByteArray[3] = byteArray[0];
        tmpByteArray[4] = byteArray[5];
        tmpByteArray[5] = byteArray[4];
        tmpByteArray[6] = byteArray[7];
        tmpByteArray[7] = byteArray[6];

        return tmpByteArray;
    }

    // Convert the GUID Hex String to a byte[]
    private static byte[] bigEndianHexStringToByteArray(String bigEndian) {
        int stringLength = bigEndian.length();
        byte[] bytes = new byte[stringLength / 2];

        for (int i = 0; i < stringLength; i += 2)
            bytes[i/2] = (byte)((Character.digit(bigEndian.charAt(i), 16) << 4) + Character.digit(bigEndian.charAt(i+1), 16));

        return bytes;
    }

    // Converts a DB Hex GUID (as byte[]) to a guid required in C#
    private static String toLittleEndian(byte[] bigEndian) {
        byte[] littleEndian = swapBytes(bigEndian);
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;

        for (byte b : littleEndian) {
            if (Arrays.asList(4, 6, 8, 10).contains(i))
                stringBuilder.append("-");

            stringBuilder.append(String.format("%02x", b));
            i++;
        }

        return stringBuilder.toString().toLowerCase();
    }

    private static byte[] littleEndianToByteArray(String littleEndian) {
        String tmpString = littleEndian.replaceAll("(\\{|\\}|-)", "");

        int stringLength = tmpString.length();
        byte[] bytes = new byte[stringLength / 2];

        for (int i = 0; i < stringLength; i += 2)
            bytes[i/2] = (byte)((Character.digit(tmpString.charAt(i), 16) << 4) + Character.digit(tmpString.charAt(i+1), 16));

        return bytes;
    }

    // Converts a guid used in C# (as byte[]) to a DB Hex GUID
    private static String toBigEndian(byte[] littleEndian) {
        byte[] bigEndian = swapBytes(littleEndian);
        StringBuilder stringBuilder = new StringBuilder();

        for (byte b : bigEndian) {
            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString().toUpperCase();
    }

    // endregion (Private Static Methods)
}