package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.errors.AuthException
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.ui.auth.AuthListener
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.Util.sha256

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
class UserRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase
) : SafeApiRequest() {

    private val users = MutableLiveData<UserDTO>()
    private val userError = MutableLiveData<String>()

    private var authListener: AuthListener? = null

    init {
        users.observeForever { user ->
            Coroutines.io {
                saveUser(user)
                return@io
            }
        }
        userError.observeForever { error_msg ->
            Coroutines.io {
                val authEx =
                    AuthException(
                        error_msg
                    )
                throw authEx
            }
        }
    }

    suspend fun getHash(): ByteArray? {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getHash()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            appDb.getUserDao().getUser()
        }
    }

    suspend fun userRegister(
        username: String,
        password: String,
        phoneNumber: String,
        IMEI: String,
        androidDevice: String
    ) {
        val authResponse =
            apiRequest { api.userRegister(androidDevice, IMEI, phoneNumber, username, password) }

        if (authResponse.errorMessage != null) {
            val authException =
                AuthException(authResponse.errorMessage)
            throw authException
        } else {
            users.postValue(authResponse.user)
        }
    }

    fun upDateUser(
        phoneNumber: String,
        IMEI: String,
        androidDevice: String,
        confirmPin: String,
        binHash: ByteArray,

        ) {
        Coroutines.io {

            appDb.getUserDao().updateUser(
                newPin = confirmPin,
                binHash = binHash,
                PHONE_NUMBER = phoneNumber,
                IMEI = IMEI,
                DEVICE = androidDevice
            ) // userId,
        }
    }

    fun upDateUserPin(confirmNewPin: String, enterOldPin: String) {
        Coroutines.io {
            appDb.getUserDao().upDateUserPin(confirmNewPin.sha256(), enterOldPin.sha256()) // userId,
        }
    }

    fun updateHash(newHash: ByteArray, oldHash: ByteArray) {
        Coroutines.io {
            appDb.getUserDao().updateUserHash(newHash, oldHash) // userId,
        }
    }

    private suspend fun saveUser(user: UserDTO) {
        Coroutines.io {

            if (!appDb.getUserDao().checkUserExists(user.userId)) {
                appDb.getUserDao().insert(user)
            }

            if (user.userRoles.isNotEmpty()) {
                for (userRole in user.userRoles) {
                    appDb.getUserRoleDao().saveRole(userRole)
                }
            }
        }
    }
}
