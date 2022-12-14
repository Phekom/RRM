package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Transaction
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.custom.errors.AuthException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
class UserRepository(
    private val api: BaseConnectionApi,
    private val appDb: AppDatabase,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : SafeApiRequest() {

    private val users = MutableLiveData<UserDTO>()
    private val userError = MutableLiveData<String>()
    private val superJob = SupervisorJob()
    private var databaseStatus: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    private var ioContext = dispatchers.io() + Job(superJob)
    private var mainContext = dispatchers.main() + Job(superJob)
    var healthState: MutableLiveData<XIEvent<XIResult<Boolean>>> = MutableLiveData()
    var databaseState: MutableLiveData<XIResult<Boolean>?> = MutableLiveData()

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
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getHash()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(dispatchers.io()) {
            appDb.getUserDao().getUser()
        }
    }

    @Suppress("TooGenericExceptionCaught")
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

    private fun postEvent(result: XIResult<Boolean>) {
        databaseStatus.postValue(XIEvent(result))
    }

    private fun postStatus(message: String) {
        val status = XIResult.Status(message)
        postEvent(status)
    }

    suspend fun getHealthCheck(): HealthCheckResponse {
        val healthCheck = apiRequest { api.healthCheck("userId") }
        return withContext(dispatchers.io()) {
            healthCheck
        }
    }

    suspend fun updateUser(
        phoneNumber: String,
        imei: String,
        androidDevice: String,
        binHash: String
    ) {
        appDb.getUserDao().updateUser(
            binHash,
            phoneNumber,
            imei,
            androidDevice
        ) // userId,
    }

    fun updateHash(newHash: String, oldHash: String) {
        appDb.getUserDao().updateUserHash(newHash, oldHash) // userId,
    }

    @Transaction
    private suspend fun saveUser(user: UserDTO) {

        if (!appDb.getUserDao().checkUserExists(user.userId)) {
            appDb.getUserDao().insert(user)
        }

        if (user.userRoles.isNotEmpty()) {
            for (userRole in user.userRoles) {
                appDb.getUserRoleDao().saveRole(userRole)
            }
        }
    }


    suspend fun authenticatePin() {
        appDb.getUserDao().pinAuthenticated()
    }

    suspend fun expirePin() {
        appDb.getUserDao().pinExpired()
    }
}
