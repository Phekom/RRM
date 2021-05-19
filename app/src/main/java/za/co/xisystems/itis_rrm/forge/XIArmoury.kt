/**
 * Created by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 11:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import androidx.security.crypto.MasterKey
import com.password4j.SecureString

object XIArmoury {
    private const val PREFS_FILE = "colors_and_styles"
    private const val NOT_SET = "NoPassphraseSet"
    private const val PASS_LENGTH = 64

    private val wizardInstance: Wizard = Wizard()
    private val scribeInstance: Scribe = Scribe()
    private val sageInstance: Sage = Sage()

    // Generate Random Passphrase
    private fun generatePassphrase(length: Int): String {
        return wizardInstance.generateRandomPassphrase(length)
    }

    // Generate Token
    fun generateToken(passphrase: SecureString): String {
        return wizardInstance.generateToken(passphrase)
    }

    // Validate Token
    fun validateToken(passphrase: SecureString, hash: String): Boolean {
        return wizardInstance.validateToken(passphrase, hash)
    }

    /**
     * Generate random secret passphrase if not set,
     * read existing if set
     */
    fun readSecretPassphrase(context: Context): String {
        val masterKey = generateMasterKey(context)
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

    fun writeEncryptedFile(context: Context, fileName: String, fileContent: ByteArray): Boolean {
        val masterKey = generateMasterKey(context)
        return scribeInstance.writeEncryptedFile(context, masterKey, fileName, fileContent)
    }

    fun readEncryptedFile(context: Context, fileName: String): ByteArray {
        val masterKey = generateMasterKey(context)
        return scribeInstance.readEncryptedFile(context, masterKey, fileName)
    }
}
