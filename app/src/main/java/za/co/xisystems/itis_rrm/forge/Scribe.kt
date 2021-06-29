/**
 * created by shaun mcdonald on 2021/05/19
 * last modified on 2021/05/19, 11:42
 * copyright (c) 2021.  xi systems  - all rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.withContext
import timber.log.Timber
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.utils.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.utils.DispatcherProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

/**
 * Create, read and write Encrypted shared preferences.
 * Read and write encrypted files
 */

class Scribe(private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()) {

    private lateinit var securePrefs: SharedPreferences
    private var _open = false
    val openForBusiness get() = _open
    companion object {
        val DIRECTORY: String = Environment.DIRECTORY_PICTURES
        const val PASS_KEY = "za.co.xisystems.itis_rmm.forge.Scribe.Passphrase"
        const val TIMESTAMP_KEY = "za.co.xisystems.itis_rmm.forge.Scribe.Timestamp"
        const val NOT_SET = "NoPassphraseSet"
    }

    /**
     * Create secure preferences
     * @param context Context
     * @param masterKey MasterKey
     * @param prefsFile String
     * @return Unit
     */
    fun initPreferences(context: Context, masterKey: MasterKey, prefsFile: String) {
        securePrefs = EncryptedSharedPreferences.create(
            context,
            prefsFile,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        _open = true
    }

    /**
     * Read / check if passphrase has been set
     * @return String
     */
    fun getPassphrase(): String {
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

    fun getTimestamp(): Long {
        return securePrefs.getLong(TIMESTAMP_KEY, 0L)
    }

    fun writePassphrase(passphrase: String) {
        with(securePrefs.edit()) {
            putString(PASS_KEY, passphrase.trim()).apply()
        }
    }

    suspend fun writeFutureTimestamp() {
        withContext(dispatchers.io()) {
            writeTimestamp(System.currentTimeMillis())
        }
    }

    fun writeTimestamp(timeInMillis: Long) {
        with(securePrefs.edit()) {
            putLong(TIMESTAMP_KEY, timeInMillis).apply()
        }
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
        fileName: String,
        fileContent: ByteArray
    ): Boolean {
        return withContext(dispatchers.io()) {
            // Creates a file with this name, or replaces an existing file
            // that has the same name. Note that the file name cannot contain
            // path separators.
            return@withContext try {
                val fileToWrite = File(DIRECTORY, fileName)
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
        }
    }

    /**
     * Read encrypted file from disk
     * @param context Context
     * @param masterKey MasterKey
     * @param fileToRead String
     * @return ByteArray
     */
    suspend fun readEncryptedFile(context: Context, masterKey: MasterKey, fileToRead: String): ByteArray {
        return withContext(dispatchers.io()) {
            val encryptedFile = EncryptedFile.Builder(
                context,
                File(DIRECTORY, fileToRead),
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

            return@withContext byteArrayOutputStream.toByteArray()
        }
    }
}
