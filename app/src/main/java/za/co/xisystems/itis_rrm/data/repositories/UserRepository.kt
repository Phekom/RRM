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

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
class UserRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase
) : SafeApiRequest() {

    private val users = MutableLiveData<UserDTO>()
    private val userError = MutableLiveData<String>()

    private var authListener: AuthListener? = null

    init {
        users.observeForever { user ->
            Coroutines.main {
                saveUser(user)
                return@main
            }
        }
        userError.observeForever { error_msg ->
            Coroutines.main {
                val authEx =
                    AuthException(
                        error_msg
                    )
                throw authEx
            }
        }
    }

    suspend fun getPin(): String {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getPin()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getUser()
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
        confirmPin: String
    ) {
        Coroutines.io {
            Db.getUserDao().updateUser(confirmPin, phoneNumber, IMEI, androidDevice) // userId,
        }
    }

    fun upDateUserPin(confirmNewPin: String, enterOldPin: String) {
        Coroutines.io {
            Db.getUserDao().upDateUserPin(confirmNewPin, enterOldPin) // userId,
        }
    }

    private suspend fun saveUser(user: UserDTO) {
        Coroutines.io {

            if (!Db.getUserDao().checkUserExists(user.userId)) {
                Db.getUserDao().insert(user)
            }

            if (user.userRoles.isNotEmpty()) {
                for (userRole in user.userRoles) {
                    Db.getUserRoleDao().saveRole(userRole)
                }
            }
        }
    }
}
