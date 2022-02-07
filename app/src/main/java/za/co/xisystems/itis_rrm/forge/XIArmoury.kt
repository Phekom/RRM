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
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.constants.Constants.FIVE_MINUTES
import za.co.xisystems.itis_rrm.constants.Constants.TEN_MINUTES
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.forge.Scribe.Companion.NOT_INITIALIZED
import za.co.xisystems.itis_rrm.utils.Coroutines

class XIArmoury private constructor(
    appContext: Context,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : LifecycleObserver {

    private var wizardInstance: Wizard = Wizard()
    private var scribeInstance: Scribe? = null
    private var sageInstance: Sage? = null
    private var masterKey: MasterKey? = null
    private val userTimestamp: AtomicLong = AtomicLong()
    private lateinit var photoFolder: File
    private lateinit var userSessionKey: SecureString
    private var armouryScope: ArmouryScope = ArmouryScope()

    companion object {
        @Volatile
        internal var instance: XIArmoury? = null
        const val PREFS_FILE = "special_styles_and_colours"
        const val NOT_SET = "NoPassphraseSet"
        const val PASS_LENGTH = 64
        private val Lock = Any()

        fun getInstance(context: Context): XIArmoury {

            return instance ?: synchronized(Lock) {
                XIArmoury(appContext = context.applicationContext)
            }.also {
                it.initArmoury(it, context)
                instance = it
            }
        }

        fun checkTimeout(): Boolean {
            return instance?.checkTimeout() == true
        }

        fun closeArmoury() {
            if (instance != null) {
                instance!!.armouryScope.destroy()
                instance!!.scribeInstance = null
                instance!!.sageInstance = null
                instance = null
            }
        }
    }

    private fun initPictureFolderAsync(appContext: Context): Deferred<File> = armouryScope.async(dispatchers.io()) {
        return@async setOrCreatePicFolder(appContext)
    }

    private suspend fun setOrCreatePicFolder(appContext: Context): File = withContext(dispatchers.io()) {
        val tempFolder = appContext
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        if (!tempFolder.exists()) {
            tempFolder.mkdirs()
        }
        return@withContext tempFolder
    }

    init {
        armouryScope.onCreate()
        this.sageInstance = Sage.getInstance(appContext)
        this.masterKey = this.sageInstance?.masterKeyAlias
        this.scribeInstance = Scribe.getInstance(
            appContext = appContext,
            sageInstance = this.sageInstance!!
        )
    }

    private fun initArmoury(instance: XIArmoury, appContext: Context) =
        armouryScope.launch(context = dispatchers.io(), start = CoroutineStart.DEFAULT) {
            instance.photoFolder = initPictureFolderAsync(appContext).await()
            val checkedPassphrase = instance.checkPassphrase(appContext)
            val readPassphrase = instance.readPassphrase()
            if (checkedPassphrase == readPassphrase) {
                instance.writeFutureTimestamp()
            } else {
                throw IllegalStateException("XIArmoury passphrases do not match!")
            }
        }

    // Generate Random Passphrase
    private fun generatePassphrase(length: Int = PASS_LENGTH): String {
        return wizardInstance.generateRandomPassphrase(length)
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
    ): XIResult<Boolean> = withContext(dispatchers.default()) {
        return@withContext wizardInstance.validateFutureToken(passphrase, hash)
    }

    private suspend fun checkPassphrase(context: Context): String = withContext(dispatchers.io()) {
        scribeInstance?.mSecurePrefs = scribeInstance?.createPreferences(
            context.applicationContext, masterKey = this@XIArmoury.masterKey!!,
            prefsFile = PREFS_FILE
        )!!
        var currentPassphrase = this@XIArmoury.scribeInstance?.getFuturePassphrase() ?: NOT_INITIALIZED
        when (currentPassphrase) {
            NOT_INITIALIZED -> {
                currentPassphrase = generatePassphrase(PASS_LENGTH)
                scribeInstance?.writeFuturePassphrase(currentPassphrase)
            }
        }
        Timber.d("$currentPassphrase", "RRM Pass phrase")
        return@withContext currentPassphrase
    }

    private fun generateMasterKey(context: Context): MasterKey? {
        return sageInstance?.generateMasterKey(context.applicationContext)
    }

    fun writeFutureTimestamp(timeInMillis: Long = System.currentTimeMillis()) = Coroutines.default {
        userTimestamp.set(timeInMillis)
    }

    private fun getTimestamp(): Long {
        return userTimestamp.get()
    }

    suspend fun writeEncryptedFile(
        context: Context,
        fileName: String,
        directory: File,
        fileContent: ByteArray
    ): Boolean = withContext(dispatchers.io()) {
        return@withContext scribeInstance!!.writeEncryptedFile(
            context.applicationContext,
            masterKey!!,
            directory,
            fileName,
            fileContent
        )
    }

    suspend fun readEncryptedFile(context: Context, fileName: String): ByteArray {
        return scribeInstance!!.readEncryptedFile(context.applicationContext, masterKey!!, photoFolder, fileName)
    }

    fun validateToken(oldTokenString: SecureString, hash: String): Boolean {
        scribeInstance!!.writeSessionKey(hash)
        val result = wizardInstance.validateToken(oldTokenString, hash)
        if (result) scribeInstance!!.writeSessionKey(hash)
        return result
    }

    fun timeStampDue(timeInMillis: Long = System.currentTimeMillis()): Boolean {
        val timeDiff = timeInMillis - getTimestamp()
        return timeDiff >= FIVE_MINUTES
    }

    fun checkTimeout(): Boolean {
        val timeInMillis = System.currentTimeMillis()
        val timeDiff = timeInMillis - getTimestamp()
        Timber.d("TimeDiff: $timeDiff")
        return timeDiff >= TEN_MINUTES
    }

    private suspend fun createUserSession(userObject: SecureString) = armouryScope.launch {
        val sessionKey = wizardInstance.generateFutureToken(userObject)
        scribeInstance!!.writeUserObject(userObject.substring(0 until userObject.length - 1))
        scribeInstance!!.writeSessionKey(sessionKey)
    }

    private fun deAuthorize() {
        scribeInstance!!.eraseSessionKey()
    }

    fun isSessionAuthorized(): Boolean {
        return scribeInstance!!.readSessionKey().isNotEmpty()
    }

    private suspend fun isAuthorized(userObject: SecureString? = null): Boolean =
        withContext(armouryScope.coroutineContext) {
            val sessionKey = scribeInstance!!.readSessionKey()
            val secId = scribeInstance!!.readUserObject()
            return@withContext sessionKey.isNotEmpty() && wizardInstance.validateToken(userObject ?: secId, sessionKey)
        }

    private fun writeUserObject(userObject: String) = armouryScope.launch {
        scribeInstance!!.writeUserObject(userObject)
    }

    fun addSecretSauce(input: CharArray, registration: Boolean) {
        val rawUserObject = input.clone().reversed().filter { char ->
            char != ' '
        }.toString()

        when {
            registration -> Coroutines.io {
                writeUserObject(rawUserObject)
                createUserSession(SecureString(rawUserObject.toCharArray()))
            }
            else -> Coroutines.io {
                val result = isAuthorized(SecureString(rawUserObject.toCharArray(), true))
                if (!result) deAuthorize()
            }
        }
    }
}





///**
// * Created by Shaun McDonald on 2021/05/19
// * Last modified on 2021/05/19, 11:43
// * Copyright (c) 2021.  XI Systems  - All rights reserved
// **/
//
//package za.co.xisystems.itis_rrm.forge
//
//import android.content.Context
//import android.os.Environment
//import androidx.lifecycle.LifecycleObserver
//import androidx.security.crypto.MasterKey
//import com.password4j.SecureString
//import kotlinx.coroutines.*
//import timber.log.Timber
//import za.co.xisystems.itis_rrm.constants.Constants.FIVE_MINUTES
//import za.co.xisystems.itis_rrm.constants.Constants.TEN_MINUTES
//import za.co.xisystems.itis_rrm.custom.results.XIResult
//import za.co.xisystems.itis_rrm.forge.Scribe.Companion.NOT_INITIALIZED
//import za.co.xisystems.itis_rrm.utils.Coroutines
//import java.io.File
//import java.util.concurrent.atomic.AtomicLong
//
//class XIArmoury private constructor(
//    appContext: Context,
//    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
//) : LifecycleObserver {
//
//    private var wizardInstance: Wizard = Wizard()
//    private var scribeInstance: Scribe? = null
//    private var sageInstance: Sage? = null
//    private var masterKey: MasterKey? = null
//    private val userTimestamp: AtomicLong = AtomicLong()
//    private lateinit var photoFolder: File
//    private lateinit var userSessionKey: SecureString
//    private var armouryScope: ArmouryScope = ArmouryScope()
//
//    companion object {
//        @Volatile
//        internal var instance: XIArmoury? = null
//        const val PREFS_FILE = "special_styles_and_colours"
//        const val NOT_SET = "NoPassphraseSet"
//        const val PASS_LENGTH = 64
//        private val Lock = Any()
//
//        fun getInstance(context: Context): XIArmoury {
//
//            return instance ?: synchronized(Lock) {
//                XIArmoury(appContext = context.applicationContext)
//            }.also {
//                it.initArmoury(it, context)
//                instance = it
//            }
//        }
//
//        fun checkTimeout(): Boolean {
//            return instance?.checkTimeout() == true
//        }
//
//        fun closeArmoury() {
//            if (instance != null) {
//                instance!!.armouryScope.destroy()
//                instance!!.scribeInstance = null
//                instance!!.sageInstance = null
//                instance = null
//            }
//        }
//    }
//
//    private fun initPictureFolderAsync(appContext: Context): Deferred<File> = armouryScope.async(dispatchers.io()) {
//        return@async setOrCreatePicFolder(appContext)
//    }
//
//    private suspend fun setOrCreatePicFolder(appContext: Context): File = withContext(dispatchers.io()) {
//        val tempFolder = appContext
//            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
//        if (!tempFolder.exists()) {
//            tempFolder.mkdirs()
//        }
//        return@withContext tempFolder
//    }
//
//    init {
//        armouryScope.onCreate()
//        this.sageInstance = Sage.getInstance(appContext)
//        this.masterKey = this.sageInstance?.masterKeyAlias
//        this.scribeInstance = Scribe.getInstance(
//            appContext = appContext,
//            sageInstance = this.sageInstance!!
//        )
//    }
//
//    private fun initArmoury(instance: XIArmoury, appContext: Context) =
//        armouryScope.launch(context = dispatchers.io(), start = CoroutineStart.DEFAULT) {
//            instance.photoFolder = initPictureFolderAsync(appContext).await()
//            val checkedPassphrase = instance.checkPassphrase(appContext)
//            val readPassphrase = instance.readPassphrase()
//            if (checkedPassphrase == readPassphrase) {
//                instance.writeFutureTimestamp()
//            } else {
//                throw IllegalStateException("XIArmoury passphrases do not match!")
//            }
//        }
//
//    // Generate Random Passphrase
//    private fun generatePassphrase(length: Int = PASS_LENGTH): String {
//        return wizardInstance.generateRandomPassphrase(length)
//    }
//
//    fun readPassphrase(): String {
//        return scribeInstance!!.readPassphrase()
//    }
//
//    // Generate Token
//    suspend fun generateFutureToken(passphrase: SecureString): String {
//        val securityToken = wizardInstance.generateFutureToken(passphrase)
//        scribeInstance?.writeSessionKey(securityToken)
//        return securityToken
//    }
//
//    // Validate Token
//    suspend fun validateFutureToken(
//        passphrase: SecureString,
//        hash: String
//    ): XIResult<Boolean> = withContext(dispatchers.default()) {
//        return@withContext wizardInstance.validateFutureToken(passphrase, hash)
//    }
//
//    private suspend fun checkPassphrase(context: Context): String = withContext(dispatchers.main()) {
//        var currentPassphrase = this@XIArmoury.scribeInstance?.getFuturePassphrase() ?: NOT_INITIALIZED
//        when (currentPassphrase) {
//            NOT_INITIALIZED -> {
//                CoroutineScope(dispatchers.io()).launch {
//                    scribeInstance?.mSecurePrefs = scribeInstance?.createPreferences(
//                        context.applicationContext, masterKey = this@XIArmoury.masterKey!!,
//                        prefsFile = PREFS_FILE
//                    )!!
//                    withContext(dispatchers.main()) {
//                        currentPassphrase = generatePassphrase(PASS_LENGTH)
//                        scribeInstance?.writeFuturePassphrase(currentPassphrase)
//                    }
//                }
//            }
//            NOT_SET -> {
//                currentPassphrase = generatePassphrase(PASS_LENGTH)
//                scribeInstance?.writeFuturePassphrase(currentPassphrase)
//            }
//        }
//
//        return@withContext currentPassphrase
//    }
//
//    private fun generateMasterKey(context: Context): MasterKey? {
//        return sageInstance?.generateMasterKey(context.applicationContext)
//    }
//
//    fun writeFutureTimestamp(timeInMillis: Long = System.currentTimeMillis()) = Coroutines.default {
//        userTimestamp.set(timeInMillis)
//    }
//
//    private fun getTimestamp(): Long {
//        return userTimestamp.get()
//    }
//
//    suspend fun writeEncryptedFile(
//        context: Context,
//        fileName: String,
//        directory: File,
//        fileContent: ByteArray
//    ): Boolean = withContext(dispatchers.io()) {
//        return@withContext scribeInstance!!.writeEncryptedFile(
//            context.applicationContext,
//            masterKey!!,
//            directory,
//            fileName,
//            fileContent
//        )
//    }
//
//    suspend fun readEncryptedFile(context: Context, fileName: String): ByteArray {
//        return scribeInstance!!.readEncryptedFile(context.applicationContext, masterKey!!, photoFolder, fileName)
//    }
//
//    fun validateToken(oldTokenString: SecureString, hash: String): Boolean {
//        scribeInstance!!.writeSessionKey(hash)
//        val result = wizardInstance.validateToken(oldTokenString, hash)
//        if (result) scribeInstance!!.writeSessionKey(hash)
//        return result
//    }
//
//    fun timeStampDue(timeInMillis: Long = System.currentTimeMillis()): Boolean {
//        val timeDiff = timeInMillis - getTimestamp()
//        return timeDiff >= FIVE_MINUTES
//    }
//
//    fun checkTimeout(): Boolean {
//        val timeInMillis = System.currentTimeMillis()
//        val timeDiff = timeInMillis - getTimestamp()
//        Timber.d("TimeDiff: $timeDiff")
//        return timeDiff >= TEN_MINUTES
//    }
//
//    private suspend fun createUserSession(userObject: SecureString) = armouryScope.launch {
//        val sessionKey = wizardInstance.generateFutureToken(userObject)
//        scribeInstance!!.writeUserObject(userObject.substring(0 until userObject.length - 1))
//        scribeInstance!!.writeSessionKey(sessionKey)
//    }
//
//    private fun deAuthorize() {
//        scribeInstance!!.eraseSessionKey()
//    }
//
//    fun isSessionAuthorized(): Boolean {
//        return scribeInstance!!.readSessionKey().isNotEmpty()
//    }
//
//    private suspend fun isAuthorized(userObject: SecureString? = null): Boolean =
//        withContext(armouryScope.coroutineContext) {
//            val sessionKey = scribeInstance!!.readSessionKey()
//            val secId = scribeInstance!!.readUserObject()
//            return@withContext sessionKey.isNotEmpty() && wizardInstance.validateToken(userObject ?: secId, sessionKey)
//        }
//
//    private fun writeUserObject(userObject: String) = armouryScope.launch {
//        scribeInstance!!.writeUserObject(userObject)
//    }
//
//    fun addSecretSauce(input: CharArray, registration: Boolean) {
//        val rawUserObject = input.clone().reversed().filter { char ->
//            char != ' '
//        }.toString()
//
//        when {
//            registration -> Coroutines.io {
//                writeUserObject(rawUserObject)
//                createUserSession(SecureString(rawUserObject.toCharArray()))
//            }
//            else -> Coroutines.io {
//                val result = isAuthorized(SecureString(rawUserObject.toCharArray(), true))
//                if (!result) deAuthorize()
//            }
//        }
//    }
//}
