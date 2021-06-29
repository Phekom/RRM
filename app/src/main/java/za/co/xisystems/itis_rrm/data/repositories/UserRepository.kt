

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
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
class UserRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase
) : SafeApiRequest() {

    private val users = MutableLiveData<UserDTO>()
    private val userError = MutableLiveData<String>()

    init {
        users.observeForever { user ->
            Coroutines.io {
                saveUser(user)
                return@io
            }
        }
        userError.observeForever { errorMsg ->
            Coroutines.io {
                val authEx =
                    AuthException(
                        errorMsg
                    )
                throw authEx
            }
        }
    }

    suspend fun getHash(): String? {
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
        imei: String,
        androidDevice: String
    ) {
        val authResponse =
            apiRequest { api.userRegister(androidDevice, imei, phoneNumber, username, password) }

        if (authResponse.errorMessage != null) {
            val authException =
                AuthException(authResponse.errorMessage)
            throw authException
        } else {
            authResponse.user?.let { concreteUser ->
                users.postValue(concreteUser)
            }
        }
    }

    suspend fun updateUser(
        phoneNumber: String,
        imei: String,
        androidDevice: String,
        binHash: String
    ) {
        Coroutines.io {

            appDb.getUserDao().updateUser(
                binHash,
                phoneNumber,
                imei,
                androidDevice
            ) // userId,
        }
    }

    fun updateHash(newHash: String, oldHash: String) {
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

    suspend fun authenticatePin() {
        appDb.getUserDao().pinAuthenticated()
    }

    suspend fun expirePin() {
        appDb.getUserDao().pinExpired()
        // AppDatabase.onAppClose()
    }
}
