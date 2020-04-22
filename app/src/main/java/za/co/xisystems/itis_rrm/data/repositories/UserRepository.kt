package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.ui.auth.AuthListener
import za.co.xisystems.itis_rrm.utils.*

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
                val authEx = AuthException(error_msg)
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
        try {
            if (authResponse.user == null) {
                userError.postValue(authResponse.errorMessage)
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

    fun upDateUser(
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
            }

            if (user.userRoles != null) {
                for (userRole in user.userRoles) {
                    Db.getUserRoleDao().saveRole(userRole)
                }

            }
        }
    }

}