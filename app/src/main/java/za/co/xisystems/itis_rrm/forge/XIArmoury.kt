/**
 * Created by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 11:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import androidx.security.crypto.MasterKey
import com.password4j.SecureString
import za.co.xisystems.itis_rrm.custom.results.XIResult

object XIArmoury {
    private const val PREFS_FILE = "colors_and_styles"
    private const val NOT_SET = "NoPassphraseSet"
    private const val PASS_LENGTH = 64

    private val wizardInstance: Wizard = Wizard()
    private val scribeInstance: Scribe = Scribe()
    private val sageInstance: Sage = Sage()

    // Generate Random Passphrase
    private fun generatePassphrase(length: Int = PASS_LENGTH): String {
        return wizardInstance.generateRandomPassphrase(length)
    }

    // Generate Token
    suspend fun generateFutureToken(passphrase: SecureString): String {
        return wizardInstance.generateFutureToken(passphrase)
    }

    // Validate Token
    suspend fun validateFutureToken(passphrase: SecureString, hash: String): XIResult<Boolean> {
        return wizardInstance.validateFutureToken(passphrase, hash)
    }

    /**
     * Generate random secret passphrase if not set,
     * read existing if set
     */
    suspend fun readFutureSecretPassphrase(context: Context): String {
        val masterKey = sageInstance.generateFutureMasterKey(context)
        scribeInstance.initPreferences(context.applicationContext, masterKey, PREFS_FILE)
        if (scribeInstance.getPassphrase() == NOT_SET) {
            val passphrase = generatePassphrase(PASS_LENGTH)
            scribeInstance.writePassphrase(passphrase)
        }
        return scribeInstance.getPassphrase()
    }

    fun readSecretPassphrase(context: Context): String {
        val masterKey = sageInstance.generateMasterKey(context)
        scribeInstance.initPreferences(context.applicationContext, masterKey, PREFS_FILE)
        if (scribeInstance.getPassphrase() == NOT_SET) {
            val passphrase = generatePassphrase(PASS_LENGTH)
            scribeInstance.writePassphrase(passphrase)
        }
        return scribeInstance.getPassphrase()
    }

    private fun generateMasterKey(context: Context): MasterKey {
        return sageInstance.generateMasterKey(context)
    }

   suspend fun writeEncryptedFile(
       context: Context,
       fileName: String,
       fileContent: ByteArray
   ): Boolean {
       val masterKey = generateMasterKey(context)
       return scribeInstance.writeEncryptedFile(context, masterKey, fileName, fileContent)
    }

    suspend fun readEncryptedFile(context: Context, fileName: String): ByteArray {
        val masterKey = generateMasterKey(context)
        return scribeInstance.readEncryptedFile(context, masterKey, fileName)
    }

    fun validateToken(oldTokenString: SecureString, hash: String): Boolean {
        return wizardInstance.validateToken(oldTokenString, hash)
    }
}
