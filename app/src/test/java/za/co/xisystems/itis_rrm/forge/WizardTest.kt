/**
 * Created by Shaun McDonald on 2021/05/22
 * Last modified on 2021/05/22, 00:31
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import com.github.ajalt.timberkt.Timber
import com.password4j.SecureString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.testutils.CoroutineTestRule

class WizardTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = CoroutineTestRule()

    @ExperimentalCoroutinesApi
    private var sut: Wizard = Wizard()

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        System.setProperty("kotlinx.coroutines.debug", "on")
        sut = Wizard(coroutinesTestRule.testDispatcherProvider)
    }

    @Test
        /**
         * SecureStrings don't reveal anything to log-hounds and  ram-snatchers
         * @return Unit
         */
    fun `it Can Secure Strings`() = runBlocking {
        val goodPassphrase = SecureString("mysteryDirector9999".toCharArray())

        try {
            println("Secure string: $goodPassphrase")
            assert(goodPassphrase.toString().contains("***"))
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            fail("It should not have thrown this exception: ${t.message}")
        }
    }

    @Test
        /**
         *
         * @return Unit
         */
    fun `it Can Erase Sources`() = runBlocking {
        try {
            val secretString = "MySecretString".toCharArray()
            val secureString = SecureString(secretString, true)
            secureString.clear()
            println("Secret string after erasure: $secretString")
            println("Secure string: $secureString")
            assertNotEquals(secretString, "MySecretString".toCharArray())
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            fail("It should not have thrown this exception: ${t.message}")
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `it Can Generate Tokens`() = runBlocking {

        try {
            val securityToken = sut.generateToken(SecureString("XiSystems@1972!".toCharArray()))
            println("security-token: $securityToken")
            assert(securityToken.isNotBlank())
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            fail("It should not have thrown this exception: ${t.message}")
        }
    }

    // This works well within the application, but sometimes fails as a unit test.
    // Needs investigation
    /**
    @ExperimentalCoroutinesApi
    @Test
    fun `it Can Validate Tokens`(): Unit = coroutinesTestRule.testDispatcher.runBlockingTest {
        withContext(coroutinesTestRule.testDispatcherProvider.default()) {
            try {
                val goodPassphrase = "XiSystems@2021!"
                val failPassphrase = "mysteriousOrange7777"
                val securityToken = sut.generateFutureToken(SecureString(goodPassphrase.toCharArray(), false))

                sut.validateFutureToken(
                    SecureString(
                        goodPassphrase.toCharArray(),
                        false
                    ),
                    securityToken
                ).also { result ->
                    when (result) {
                        is XISuccess<Boolean> -> {
                            assertEquals(true, result.data)
                            println("Legit cmp passed")
                        }
                        is XIError -> {
                            fail(result.message)
                        }
                        else -> {
                            fail("$result")
                        }
                    }
                }

                sut.validateFutureToken(
                    SecureString(
                        failPassphrase.toCharArray(),
                        false
                    ),
                    securityToken
                ).also { result ->

                    when (result) {
                        is XISuccess<Boolean> -> {
                            assertEquals(false, result.data)
                            println("Bogus cmp passed")
                        }
                        is XIError -> {
                            fail(result.message)
                        }
                        else -> {
                            fail("$result")
                        }
                    }
                }

                assertTrue(securityToken.isNotEmpty())
            } catch (t: Throwable) {
                val message = t.message ?: XIErrorHandler.UNKNOWN_ERROR
                Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
                fail("It should not have thrown this exception: $message")
            }
        }
    }
    */

    @ExperimentalCoroutinesApi
    @Test
        /**
         * Unique random passphrases are used to secure data at rest
         * @return Unit
         */
    fun `it Can Generate Random Passphrase`() {

        try {
            val randomPhrase = sut.generateRandomPassphrase(64)
            assertTrue(randomPhrase.length == 64)
            // Regex to check alphanumeric string
            assertTrue(randomPhrase.matches("^[a-zA-Z0-9]*$".toRegex()))
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            fail("It should not have thrown this exception: ${t.message}")
        }
    }
}
