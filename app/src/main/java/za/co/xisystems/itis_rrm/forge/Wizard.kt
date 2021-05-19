/**
 * Created by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 10:45
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import com.github.ajalt.timberkt.Timber
import com.password4j.Argon2Function
import com.password4j.Password
import com.password4j.SecureString
import com.password4j.types.Argon2
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import java.security.SecureRandom

/**
 * Wizard generates and validates tokens
 * along with random pass phrases of variable lengths
 */
class Wizard {
    companion object {
        // Argon2 implementation improvement
        private const val wizardSaltPile = 64
        private const val memory = 4096
        private const val threads = 4
        private const val outputLength = 128
        private const val iterations = 20
        private const val version = 19
        private val type = Argon2.ID
    }

    /**
     * Custom Argon2 implementation to increase resistance to GPU
     * and time-based attacks
     */
    private val argon2Function: Argon2Function by lazy {
        Argon2Function.getInstance(memory,
            iterations, threads,
            outputLength,
            type, version)
    }

    /**
     * Generate hash with custom argon2Function
     * @param passphrase SecureString
     * @return String
     */
    fun generateToken(passphrase: SecureString): String {
        val hash = Password.hash(passphrase).addRandomSalt(wizardSaltPile).with(argon2Function)
        return hash.result
    }

    /**
     * Validate token by derive Argon2 instance from hash!
     * Handy when better implementations are used.
     * @param passphrase SecureString
     * @param hash String
     * @return Boolean
     */
    fun validateToken(passphrase: SecureString, hash: String): Boolean {
        return try {
            Password.check(passphrase, hash).with(Argon2Function.getInstanceFromHash(hash))
        } catch (t: Throwable) {
            Timber.e(t) { t.message ?: XIErrorHandler.UNKNOWN_ERROR }
            false
        }
    }

    /**
     * Randmon alphanumeric passphrase
     * @param size Int
     * @return String
     */
    fun generateRandomPassphrase(size: Int): String {

        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9').shuffled()
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)

        return (bytes.indices)
            .map {
                charPool[random.nextInt(charPool.size)]
            }.joinToString("")
    }
}
