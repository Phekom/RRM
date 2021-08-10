/**
 * Created by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 11:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import android.os.Environment
import androidx.lifecycle.LifecycleObserver
import androidx.security.crypto.MasterKey
import com.password4j.SecureString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.constants.Constants.TEN_MINUTES
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.forge.Scribe.Companion.NOT_INITIALIZED
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class XIArmoury private constructor(
    context: Context
) : LifecycleObserver {

    private val wizardInstance: Wizard = Wizard()
    private var scribeInstance: Scribe? = null
    private val sageInstance: Sage = Sage()
    private var masterKey: MasterKey? = null
    private val userTimestamp: AtomicLong = AtomicLong()
    private var photoFolder: File
    private lateinit var userSessionKey: SecureString
    private var armouryScope = ArmouryScope()

    companion object {
        @Volatile
        private var instance: XIArmoury? = null
        const val PREFS_FILE = "specialstylesandcolours"
        const val NOT_SET = "NoPassphraseSet"
        const val PASS_LENGTH = 64
        private val Lock = Any()

        fun getInstance(appContext: Context): XIArmoury {
            return instance ?: synchronized(Lock) {
                XIArmoury(context = appContext)
            }.also {
                instance = it
                instance!!
            }
        }
    }

    private suspend fun initPictureFolder(context: Context): File = withContext(Dispatchers.IO) {
        return@withContext setOrCreatePicFolder(context)
    }

    private fun setOrCreatePicFolder(context: Context): File {
        val tempFolder = context.applicationContext
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (!tempFolder.exists()) {
            tempFolder.mkdirs()
        }
        return tempFolder
    }

    init {
        this.masterKey = generateMasterKey(context)
        Scribe(context = context, masterKey = masterKey!!).also { this.scribeInstance = it }

        this.photoFolder = setOrCreatePicFolder(context)
        this.generatePassphrase(PASS_LENGTH)
        armouryScope.onCreate()
        this.initArmoury(context)
    }

    private fun initArmoury(context: Context) = armouryScope.launch(armouryScope.coroutineContext) {
        this@XIArmoury.photoFolder = initPictureFolder(context)
        val checkedPassphrase = this@XIArmoury.checkPassphrase(context)
        val readPassphrase = this@XIArmoury.readPassphrase()
        if (checkedPassphrase == readPassphrase) {
            this@XIArmoury.writeFutureTimestamp()
        } else {
            throw IllegalStateException("XIArmoury passphrases do not match!")
        }
    }

    // Generate Random Passphrase
    private fun generatePassphrase(length: Int = PASS_LENGTH): String {
        val passphrase = wizardInstance.generateRandomPassphrase(length)
        scribeInstance!!.writePassphrase(passphrase)
        return scribeInstance!!.readPassphrase()
    }

    fun readPassphrase(): String {
        return scribeInstance!!.readPassphrase()
    }

    // Generate Token
    suspend fun generateFutureToken(passphrase: SecureString): String {
        val securityToken = wizardInstance.generateFutureToken(passphrase)
        scribeInstance?.writeSessionKey(securityToken)
        return securityToken
    }

    // Validate Token
    suspend fun validateFutureToken(
        passphrase: SecureString,
        hash: String
    ): XIResult<Boolean> = withContext(Dispatchers.Default) {
        return@withContext wizardInstance.validateFutureToken(passphrase, hash)
    }

    private suspend fun initPreferences(
        context: Context,
        masterKey: MasterKey,
        prefsFile: String
    ): Boolean =
        withContext(Dispatchers.Default) {
            scribeInstance?.initPreferences(context, masterKey, prefsFile)
            return@withContext scribeInstance?.operational ?: false
        }

    private fun writePassphrase(passphrase: String) {
        this.scribeInstance?.writePassphrase(passphrase)
    }

    suspend fun checkPassphrase(context: Context): String = withContext(Dispatchers.Default) {
        var currentPassphrase = this@XIArmoury.scribeInstance?.getPassphrase() ?: NOT_INITIALIZED
        when (currentPassphrase) {
            NOT_INITIALIZED -> {
                scribeInstance?.securePrefs = scribeInstance?.createPreferences(
                    context, masterKey = this@XIArmoury.masterKey!!,
                    prefsFile = PREFS_FILE
                )!!
                currentPassphrase = generatePassphrase(PASS_LENGTH)
            }
            NOT_SET -> {
                currentPassphrase = generatePassphrase(PASS_LENGTH)
            }
        }

        return@withContext currentPassphrase
    }

    private fun generateMasterKey(context: Context): MasterKey {
        return sageInstance.generateMasterKey(context)
    }

    fun writeFutureTimestamp(timeInMillis: Long = System.currentTimeMillis()) = Coroutines.default {
        userTimestamp.set(timeInMillis)
    }

    fun getTimestamp(): Long {
        return userTimestamp.get()
    }

    suspend fun writeEncryptedFile(
        context: Context,
        fileName: String,
        directory: File,
        fileContent: ByteArray
    ): Boolean = withContext(Dispatchers.Default) {
        return@withContext scribeInstance!!.writeEncryptedFile(
            context,
            masterKey!!,
            directory,
            fileName,
            fileContent
        )
    }

    suspend fun readEncryptedFile(context: Context, fileName: String): ByteArray {
        return scribeInstance!!.readEncryptedFile(context, masterKey!!, photoFolder, fileName)
    }

    fun validateToken(oldTokenString: SecureString, hash: String): Boolean {
        scribeInstance!!.writeSessionKey(hash)
        val result = wizardInstance.validateToken(oldTokenString, hash)
        if (result) scribeInstance!!.writeSessionKey(hash)
        return result
    }

    fun checkTimeout(): Boolean {
        val timeInMillis = System.currentTimeMillis()
        val timeDiff = timeInMillis - userTimestamp.get()
        Timber.d("TimeDiff: $timeDiff")
        return timeDiff >= TEN_MINUTES
    }

    suspend fun createUserSession(userObject: SecureString) = armouryScope.launch {
        val sessionKey = wizardInstance.generateFutureToken(userObject)
        scribeInstance!!.writeUserObject(userObject.substring(0 until userObject.length - 1))
        scribeInstance!!.writeSessionKey(sessionKey)
    }

    fun deAuthorize() {
        scribeInstance!!.writeSessionKey("")
    }

    suspend fun isAuthorized(): Boolean = withContext(armouryScope.coroutineContext) {
        val sessionKey = scribeInstance!!.readSessionKey()
        return@withContext sessionKey.isNotEmpty()
    }

    fun writeUserObject(userObject: String) = armouryScope.launch {
        scribeInstance!!.writeUserObject(userObject)
    }
}
