/**
 * Created by Shaun McDonald on 2021/05/22
 * Last modified on 2021/05/22, 00:31
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import com.github.ajalt.timberkt.Timber
import com.password4j.SecureString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.testutils.CoroutineTestRule

@RunWith(RobolectricTestRunner::class)

class WizardTest {



    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()
    private val sut: Wizard = Wizard(coroutinesTestRule.testDispatcherProvider)
    @Test
    fun `it Can Secure Strings`() {
        val goodPassphrase = SecureString("mysterydirector9999".toCharArray())

        try {
            println("Secure string: $goodPassphrase")
            assert(goodPassphrase.toString().contains("***"))
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            Assert.fail("It should not have thrown an exception")
        }
    }

    @Test
    fun `it Can Erase Sources`() {
        try {
            val secretString = "MySecretString".toCharArray()
            val secureString = SecureString(secretString, true)
            secureString.clear()
            println("Secret string: $secretString")
            println("Secure string: $secureString")
            Assert.assertNotEquals(secretString, "MySecretString".toCharArray())
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            Assert.fail("It should not have thrown an exception")
        }
    }

    @Test
    fun `it Can Generate Tokens`() {

        try {
            val securityToken = sut.generateToken(SecureString("XiSystems@1972!".toCharArray()))
            println("security-token: $securityToken")
            assert(securityToken.isNotBlank())
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            Assert.fail("It should not have thrown an exception")
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `it Can Validate Tokens`() = coroutinesTestRule.testDispatcher.runBlockingTest {

        try {
            val goodPassphrase = SecureString("mysterydirector9999".toCharArray())
            val failPassphrase = SecureString("mysteriousorange7777".toCharArray())
            val securityToken = sut.generateFutureToken(goodPassphrase)

            println("Security Token: $securityToken")
            var isAuthd =
                sut.validateFutureToken(failPassphrase, securityToken)
            Assert.assertFalse(isAuthd)

            isAuthd =
                sut.validateFutureToken(goodPassphrase, securityToken)

            Assert.assertTrue(isAuthd)
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            println("$t")
            Assert.fail("It should not have thrown an exception")
        }
    }

    @Test
    fun `it Can Generate Random Passphrase`() {

        try {
            val randomPhrase = sut.generateRandomPassphrase(64)
            Assert.assertTrue(randomPhrase.length == 64)
            println(randomPhrase)
            // Regex to check alphanumeric string
            Assert.assertTrue(randomPhrase.matches("^[a-zA-Z0-9]*$".toRegex()))
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            Assert.fail("It should not have thrown an exception: ${t.message}")
        }
    }
}
