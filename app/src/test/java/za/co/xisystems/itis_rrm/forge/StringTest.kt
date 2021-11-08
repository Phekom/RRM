/**
 * Created by Shaun McDonald on 2021/05/27
 * Last modified on 27/05/2021, 12:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import com.password4j.SecureString
import java.nio.CharBuffer
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import za.co.xisystems.itis_rrm.utils.Utils

class StringTest {
    @Before
    fun setup() {
        Utils.setup()
    }

    @Test
    fun testConstructors() {
        // GIVEN
        val password = charArrayOf('a', 'b', 'c', 'd', 'e', 'f')

        // WHEN
        val ss = SecureString(password)
        val sub1 = ss.subSequence(1, 4)
        val sub2 = SecureString(password, 1, 4)

        // THEN
        Assert.assertEquals(sub1.length.toLong(), sub2.length.toLong())
        Assert.assertEquals(
            charArrayOf('b', 'c', 'd').contentToString(),
            Utils.fromCharSequenceToChars(sub1).contentToString()
        )
        Assert.assertEquals(
            Utils.fromCharSequenceToChars(sub1).contentToString(),
            Utils.fromCharSequenceToChars(sub2).contentToString()
        )
    }

    @Test
    fun testClear() {
        // GIVEN
        val password = charArrayOf('a', 'b', 'c', 'd')

        // WHEN
        val ss = SecureString(password)
        ss.clear()

        // THEN
        val z = Character.MIN_VALUE
        Assert.assertEquals(
            charArrayOf(z, z, z, z).contentToString(),
            Utils.fromCharSequenceToChars(ss).contentToString()
        )
    }

    @Test(expected = NullPointerException::class)
    fun testNull() {
        SecureString(null)
    }

    @Test(expected = NullPointerException::class)
    fun testNull2() {
        SecureString(null, 0, 0)
    }

    @Test(expected = ArrayIndexOutOfBoundsException::class)
    fun testOut1() {
        SecureString(charArrayOf('a', 'b', 'c', 'd'), 0, 10)
    }

    @Test(expected = NegativeArraySizeException::class)
    fun testOut2() {
        SecureString(charArrayOf('a', 'b', 'c', 'd'), 0, -4)
    }

    @Test(expected = NegativeArraySizeException::class)
    fun testOut3() {
        SecureString(charArrayOf('a', 'b', 'c', 'd'), 3, 0)
    }

    @Test
    fun testEmpty() {
        val ss = SecureString(CharArray(0))
        Assert.assertEquals("SecureString[****]", ss.toString())
        Assert.assertEquals(0, ss.length.toLong())
        try {
            ss[0]
            Assert.fail()
        } catch (e: ArrayIndexOutOfBoundsException) {
            Assert.assertTrue(true)
        }
    }

    @Test
    fun testToString() {
        val ss = SecureString(charArrayOf('a', 'b', 'c', 'd'))
        Assert.assertEquals("SecureString[****]", ss.toString())
    }

    @Test
    fun erase() {
        // GIVEN
        val password1 = charArrayOf('a', 'b', 'c', 'd')
        val password2 = charArrayOf('a', 'b', 'c', 'd')

        // WHEN
        SecureString(password1, true)
        SecureString(password2, false)

        // THEN
        val z = Character.MIN_VALUE
        Assert.assertEquals(charArrayOf(z, z, z, z).contentToString(), password1.contentToString())
        Assert.assertEquals(charArrayOf('a', 'b', 'c', 'd').contentToString(), password2.contentToString())
    }

    @Test
    fun testEquality() {
        val password = charArrayOf('a', 'b', 'c', 'd')
        val str = String(password)
        val ss = SecureString(password)
        val ss2 = Utils.append(ss, "123") as SecureString
        Assert.assertTrue(ss as CharSequence == str && ss as CharSequence == CharBuffer.wrap(password))
        Assert.assertNotEquals(null, ss)
        Assert.assertNotEquals("cbad", ss)
        Assert.assertEquals(SecureString(password), ss)
        Assert.assertNotEquals(SecureString(charArrayOf('b', 'b', 'b', 'b')), ss)
        Assert.assertEquals(ss, ss)
        Assert.assertEquals(password.contentHashCode().toLong(), ss.hashCode().toLong())
        Assert.assertNotEquals(ss, 123)
        Assert.assertNotEquals(ss2, ss)
    }

    @Test
    fun testUtilities() {
        val c1: CharArray = Utils.fromCharSequenceToChars(null)
        val c2: CharArray = Utils.fromCharSequenceToChars(String(CharArray(0)))
        val b1: ByteArray = Utils.fromCharSequenceToBytes(null)
        val b2: ByteArray = Utils.fromCharSequenceToBytes(String(CharArray(0)))
        val b3: ByteArray = Utils.fromCharSequenceToBytes(String(charArrayOf(1.toChar())))
        val cs1: CharSequence? = Utils.append("a", null)
        val cs2: CharSequence? = Utils.append(null, "b")
        val a1: CharSequence? = Utils.append(SecureString(charArrayOf('a', 'b', 'c')), "def")
        val a2: CharSequence? = Utils.append(null, "def")
        val a3: CharSequence? = Utils.append(SecureString(CharArray(0)), "def")
        val a4: CharSequence? = Utils.append("abc", null)
        val a5: CharSequence? = Utils.append("abc", SecureString(CharArray(0)))
        Assert.assertEquals(c1.contentToString(), c2.contentToString())
        Assert.assertEquals(b1.contentToString(), b2.contentToString())
        Assert.assertEquals(1, b3.size.toLong())
        Assert.assertEquals(1, b3[0].toInt())
        Assert.assertEquals("a", cs1)
        Assert.assertEquals("b", cs2)
        Assert.assertEquals(
            charArrayOf('a', 'b', 'c', 'd', 'e', 'f').contentToString(),
            Utils.fromCharSequenceToChars(a1).contentToString()
        )
        Assert.assertEquals(
            charArrayOf('d', 'e', 'f').contentToString(),
            Utils.fromCharSequenceToChars(a2).contentToString()
        )
        Assert.assertEquals(
            charArrayOf('d', 'e', 'f').contentToString(),
            Utils.fromCharSequenceToChars(a3).contentToString()
        )
        Assert.assertEquals(
            charArrayOf('a', 'b', 'c').contentToString(),
            Utils.fromCharSequenceToChars(a4).contentToString()
        )
        Assert.assertEquals(
            charArrayOf('a', 'b', 'c').contentToString(),
            Utils.fromCharSequenceToChars(a5).contentToString()
        )
    }
}
