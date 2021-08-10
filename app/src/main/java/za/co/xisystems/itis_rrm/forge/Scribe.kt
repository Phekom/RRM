/**
 * created by shaun mcdonald on 2021/05/19
 * last modified on 2021/05/19, 11:42
 * copyright (c) 2021.  xi systems  - all rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.WorkerThread
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.password4j.SecureString
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.forge.XIArmoury.Companion.PREFS_FILE
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Create, read and write Encrypted shared preferences.
 * Read and write encrypted files
 */

class Scribe(
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    masterKey: MasterKey,
    context: Context
) {

    var securePrefs: SharedPreferences
    private var armouryScope = ArmouryScope()
    val operational get() = true

    init {
        this.securePrefs = createPreferences(context = context, masterKey = masterKey)
    }

    companion object {
        const val PASS_KEY = "za.co.xisystems.itis_rmm.forge.Scribe.Passphrase"
        const val SESSION_KEY = "za.co.xisystems.itis_rrm.forge.Scribe.SessionKey"
        const val USER_KEY = "za.co.xisystems.itis_rrm.forge.Scribe.UserKey"
        const val NOT_SET = "NoPassphraseSet"
        const val NOT_INITIALIZED: String = "NoInitialization"
    }

    /**
     * Create secure preferences
     * @param context Context
     * @param masterKey MasterKey
     * @param prefsFile String
     * @return Unit
     */
    suspend fun initPreferences(
        context: Context,
        masterKey: MasterKey,
        prefsFile: String = "specialstylesandcolours"
    ): SharedPreferences = withContext(dispatchers.io()) {
        return@withContext createPreferences(context, prefsFile, masterKey)
    }

    suspend fun latePreferences(
        context: Context,
        masterKey: MasterKey,
        prefsFile: String = PREFS_FILE
    ): Scribe = withContext(dispatchers.io()) {
        this@Scribe.securePrefs = initPreferences(context, masterKey, prefsFile)
        return@withContext this@Scribe
    }

    @WorkerThread
    fun createPreferences(
        context: Context,
        prefsFile: String = "specialstylesandcolours",
        masterKey: MasterKey
    ): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context.applicationContext,
            prefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Read / check if passphrase has been set
     * @return String
     */
    suspend fun getPassphrase(): String = withContext(dispatchers.io()) {
        return@withContext readPassphrase()
    }

    @WorkerThread
    fun readPassphrase(): String {
        return securePrefs.getString(PASS_KEY, NOT_SET)!!
    }

    /**
     * Save random passphrase
     * @param passphrase String
     * @return Unit
     */
    suspend fun writeFuturePassphrase(passphrase: String) {
        withContext(dispatchers.io()) {
            writePassphrase(passphrase)
        }
    }

    fun writePassphrase(passphrase: String) {
        with(securePrefs.edit()) {
            putString(PASS_KEY, passphrase.trim()).apply()
        }
    }

    fun writeSessionKey(sessionKey: String) {
        with(securePrefs.edit()) {
            putString(SESSION_KEY, sessionKey).apply()
        }
    }

    fun readSessionKey(): SecureString {
        val nakedKey = securePrefs.getString(SESSION_KEY, "UNAUTHORIZED")
        return SecureString(nakedKey!!.toCharArray())
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

    fun writeUserObject(userObject: String) {
        with(securePrefs.edit()) {
            putString(USER_KEY, userObject).apply()
        }
    }

    fun readUserObject(): String {
        return securePrefs.getString(USER_KEY, "UNAUTHORIZED")!!
    }
}
