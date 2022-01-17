/**
 * created by shaun mcdonald on 2021/05/19
 * last modified on 2021/05/19, 11:42
 * copyright (c) 2021.  xi systems  - all rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import android.content.SharedPreferences
import android.os.StrictMode
import androidx.annotation.WorkerThread
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.password4j.SecureString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.utils.DispatcherProvider

/**
 * Create, read and write Encrypted shared preferences.
 * Read and write encrypted files
 */

class Scribe private constructor(
    context: Context,
    sageInstance: Sage,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {

    var mSecurePrefs: SharedPreferences? = null
    private val securedPrefs get() = mSecurePrefs!!
    private var armouryScope = ArmouryScope()
    private var mOperational: Boolean = mSecurePrefs != null

    val operational get() = mOperational

    init {
        this.mSecurePrefs =
            createPreferences(context = context, masterKey = sageInstance.masterKeyAlias)
    }

    companion object {
        @Volatile
        private var instance: Scribe? = null
        private val Lock = Any()
        const val PASS_KEY = "za.co.xisystems.itis_rmm.forge.Scribe.Passphrase"
        const val SESSION_KEY = "za.co.xisystems.itis_rrm.forge.Scribe.SessionKey"
        const val USER_KEY = "za.co.xisystems.itis_rrm.forge.Scribe.UserKey"
        const val NOT_SET = "NoPassphraseSet"
        const val NOT_INITIALIZED: String = "NoInitialization"
        const val PREFS_FILE = "special_styles_and_colours"

        fun getInstance(
            appContext: Context,
            sageInstance: Sage
        ): Scribe {
            return instance ?: synchronized(Lock) {
                Scribe(context = appContext, sageInstance = sageInstance).also {
                    instance = it
                }
            }
        }
    }

    /**
     * Create secure preferences
     * @param context Context
     * @param masterKey MasterKey
     * @param prefsFile String
     * @return Unit
     */
    fun initSecurePrefsAsync(
        context: Context,
        masterKey: MasterKey,
        prefsFile: String = PREFS_FILE
    ): Deferred<SharedPreferences> = armouryScope.async(dispatchers.io()) {
        return@async createPreferences(context, prefsFile, masterKey)
    }

    @WorkerThread
    fun createPreferences(
        context: Context,
        prefsFile: String = PREFS_FILE,
        masterKey: MasterKey
    ): SharedPreferences {
        StrictMode.allowThreadDiskReads()
        StrictMode.allowThreadDiskWrites()
        val preferences = EncryptedSharedPreferences.create(
            context,
            prefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        mOperational = true
        return preferences
    }

    /**
     * Read / check if passphrase has been set
     * @return String
     */
    suspend fun getFuturePassphrase(): String = withContext(dispatchers.io()) {
        return@withContext readPassphrase()
    }

    @WorkerThread
    fun readPassphrase(): String {
        return securedPrefs.getString(PASS_KEY, NOT_SET)!!
    }

    /**
     * Save random passphrase
     * @param passphrase String
     * @return Unit
     */
    suspend fun writeFuturePassphrase(passphrase: String) = withContext(dispatchers.io()) {
        writePassphrase(passphrase)
    }

    private fun writePassphrase(passphrase: String) = with(securedPrefs.edit()) {
        putString(PASS_KEY, passphrase.trim()).commit()
    }

    fun writeSessionKey(sessionKey: String) = with(securedPrefs.edit()) {
        putString(SESSION_KEY, sessionKey).commit()
    }

    fun readSessionKey(): String {
        return securedPrefs.getString(SESSION_KEY, "").orEmpty()
    }

    fun writeUserObject(userObject: String) = dispatchers.io().asExecutor().execute {
        with(securedPrefs.edit()) {
            putString(USER_KEY, userObject).apply()
        }
    }

    suspend fun readUserObject(): SecureString = withContext(dispatchers.io()) {
        val nakedObject = securedPrefs.getString(USER_KEY, "")!!
        return@withContext SecureString(nakedObject.toCharArray())
    }

    /**
     * Write encrypted image to disk
     * @param context Context
     * @param masterKey MasterKey
     * @param fileName String
     * @param fileContent ByteArray
     * @return Unit
     */
    suspend fun writeEncryptedFile(
        context: Context,
        masterKey: MasterKey,
        directory: File,
        fileName: String,
        fileContent: ByteArray
    ): Boolean {
        return withContext(dispatchers.io()) {
            // Creates a file with this name, or replaces an existing file
            // that has the same name. Note that the file name cannot contain
            // path separators.
            return@withContext writeFile(directory, fileName, context, masterKey, fileContent)
        }
    }

    /**
     * WorkerThread-bound function to write encrypted file to disk
     * @param directory File
     * @param fileName String
     * @param context Context
     * @param masterKey MasterKey
     * @param fileContent ByteArray
     * @return Boolean
     */
    @WorkerThread
    private fun writeFile(
        directory: File,
        fileName: String,
        context: Context,
        masterKey: MasterKey,
        fileContent: ByteArray
    ) = try {
        val fileToWrite = File(directory, fileName)
        val encryptedFile = EncryptedFile.Builder(
            context.applicationContext,
            fileToWrite,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        // File cannot exist before using openFileOutput
        if (fileToWrite.exists()) {
            fileToWrite.delete()
        }

        encryptedFile.openFileOutput().apply {
            write(fileContent)
            flush()
            close()
        }
        true
    } catch (t: IOException) {
        val cause = "Failed to write $fileName: ${t.message ?: XIErrorHandler.UNKNOWN_ERROR}"
        Timber.e(t, cause)
        false
    }

    /**
     * Read encrypted file from disk
     * @param context Context
     * @param masterKey MasterKey
     * @param fileToRead String
     * @return ByteArray
     */
    suspend fun readEncryptedFile(
        context: Context,
        masterKey: MasterKey,
        directory: File,
        fileToRead: String
    ): ByteArray {
        return withContext(dispatchers.io()) {
            return@withContext readFile(context, directory, fileToRead, masterKey)
        }
    }

    /**
     * WorkerThread bound function to read encrypted file from disk
     * @param context Context
     * @param directory File
     * @param fileToRead String
     * @param masterKey MasterKey
     * @return ByteArray
     */
    @WorkerThread
    private fun readFile(
        context: Context,
        directory: File,
        fileToRead: String,
        masterKey: MasterKey
    ): ByteArray {
        val encryptedFile = EncryptedFile.Builder(
            context.applicationContext,
            File(directory, fileToRead),
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        val inputStream = encryptedFile.openFileInput()
        val byteArrayOutputStream = ByteArrayOutputStream()
        var nextByte: Int = inputStream.read()
        while (nextByte != -1) {
            byteArrayOutputStream.write(nextByte)
            nextByte = inputStream.read()
        }

        return byteArrayOutputStream.toByteArray()
    }

    fun eraseSessionKey() {
        with(securedPrefs.edit()) {
            remove(SESSION_KEY).apply()
        }
    }
}
