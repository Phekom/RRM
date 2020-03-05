package za.co.xisystems.itis_rrm.data.repositories

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.ui.auth.AuthListener
import za.co.xisystems.itis_rrm.utils.ApiException
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.NoConnectivityException
import za.co.xisystems.itis_rrm.utils.NoInternetException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
class UserRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase
) : SafeApiRequest() {

    private val users = MutableLiveData<UserDTO>()
    private val user_error = MutableLiveData<String>()

    private var authListener: AuthListener? = null

    init {
        users.observeForever { user ->
            Coroutines.main {
                saveUser(user)
                return@main
            }
        }
        user_error.observeForever { error_msg ->
            Coroutines.main {
                sendError(error_msg)
            }
        }

    }

    private suspend fun sendError(errorMsg: String?) {
        return withContext(Dispatchers.IO) {
//            val failed = errorMsg
        }

    }

    suspend fun getPin(): String {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getPin()
        }
    }
    suspend fun getUserRoles(): LiveData<List<UserRoleDTO>> {
        return withContext(Dispatchers.IO) {
            Db.getUserRoleDao().getRoles()
        }
    }
    suspend fun getPin2(confirmPin: String?): String {
        return withContext(Dispatchers.IO) {
         confirmPin.toString()
        }
    }

    suspend fun getUser(): LiveData<UserDTO> {
        return withContext(Dispatchers.IO) {
            Db.getUserDao().getuser()
        }
    }

    suspend fun userRegister(
        username: String,
        password: String,
        phoneNumber: String,
        IMEI: String,
        androidDevice: String
    ) {

//        val IMEI = "7436738"
//        val phoneNumber = ""
//        val androidDevice = "svcc"

        val authResponse =
            apiRequest { api.userRegister(androidDevice, IMEI, phoneNumber, username, password) }
        try {
            if (authResponse.user == null) {
                user_error.postValue(authResponse.errorMessage)
            } else {
                users.postValue(authResponse.user)

            }


        } catch (e: ApiException) {
            authListener?.onFailure(e.message!!)
        } catch (e: NoInternetException) {
            authListener?.onFailure(e.message!!)
        } catch (e: NoConnectivityException) {
            authListener?.onFailure(e.message!!)
        }

    }

    fun upDateUser(     //userId: String,
                                 phoneNumber: String,
                                 IMEI: String,
                                 androidDevice: String,
                                 confirmPin: String) {
        Coroutines.io {
                Db.getUserDao().updateUser( confirmPin, phoneNumber, IMEI, androidDevice)//userId,
        }
    }

    fun upDateUserPin(confirmNewPin: String, enterOldPin: String) {
        Coroutines.io {
            Db.getUserDao().upDateUserPin( confirmNewPin, enterOldPin)//userId,
        }
    }

    private suspend fun saveUser(user: UserDTO) {
        Coroutines.io {

            if (!Db.getUserDao().checkUserExists(user.userId)) {
                Db.getUserDao().insert(user)

            }else{
                // TODO: What are we planning to do here
//                Db.getUserDao().updateUser( PIN, phoneNumber, IMEI, androidDevice,user.WEB_SERVICE_URI)
            }

            if (user.userRoles != null) {
                for (userRole in user.userRoles) {
                    Db.getUserRoleDao().saveRole(userRole)
                }

            }
        }
    }


//    fun updateRegistrationInfo(
//        webServiceUri: String?,
//        userId: String?,
//        registrationId: String?,
//        pin: String?
//    ) {
//        if (webServiceUri != null)
//            registrationInfoDataSource.updateWebServiceUri(webServiceUri)
//
//        if (registrationId != null)
//            registrationInfoDataSource.updateRegistrationId(registrationId)
//
//        if (userId != null)
//            registrationInfoDataSource.updateUserId(userId)
//
//        if (pin != null)
//            registrationInfoDataSource.updatePin(pin)
//    }

    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ChronoUnit.MINUTES.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    }


//    suspend fun saveUser(userDTO: UserDTO) = Db.getUserDao().insert(userDTO)

//    fun getUser() = Db.getUserDao().getuser()

    suspend fun removeUser(userDTO: UserDTO) = Db.getUserDao().removeUser(userDTO)
    fun clearUser() = Db.getUserDao().deleteUser()



}