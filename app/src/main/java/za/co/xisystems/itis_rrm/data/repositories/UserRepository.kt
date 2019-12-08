package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.RoleResponseListener
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.network.responses.AuthResponse
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.ApiException
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.NoInternetException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
class UserRepository(
    private val api : BaseConnectionApi,
    private val Db : AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest(){

    private val userRoles = MutableLiveData<List<UserRoleDTO>>()
    var roleResponseListener: RoleResponseListener? = null
    init {
        userRoles.observeForever {
            roleResponseListener?.onRoleSuccess(it)
            saveRoles(it)
        }
    }

    private fun saveRoles(userRoles: List<UserRoleDTO>) {
        Coroutines.io {
                for (role in userRoles) {
                    Db.getUserRoleDao().saveRole(role)
                }
//            Db.getUserRoleDao().saveAllRoles(userRoles)
        }
    }

    suspend fun getUserRoles() : LiveData<List<UserRoleDTO>> {
        return withContext(Dispatchers.IO){
            val userId = Db.getUserDao().getuserID()

            fetchUserRoles(userId)
            Db.getUserRoleDao().getRoles()
        }
    }

    suspend fun userRegister(username: String, password: String ): AuthResponse {

        val IMEI = "7436738"
        val phoneNumber = ""
        val androidDevice = "svcc"
        return apiRequest { api.userRegister(androidDevice, IMEI, phoneNumber, username, password) }
//        return apiRequest { BaseConnectionApi().userRegister(androidDevice, IMEI, phoneNumber, username, password) }
    }

    private suspend fun fetchUserRoles(userId : String){
        val lastSavedAt = prefs.getLastSavedAt()
        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {
            try {
                val userRoleResponse = apiRequest { api.userRoles(userId) }
                userRoleResponse.userRoles.let {
                    roleResponseListener?.onRoleSuccess(it)
                    saveRoles(it)
                }
                roleResponseListener?.onFailure(userRoleResponse.errorMessage)
            } catch (e: ApiException) {
                roleResponseListener?.onFailure(e.message!!)
            } catch (e: NoInternetException) {
                roleResponseListener?.onFailure(e.message!!)
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
        return ChronoUnit.MINUTES.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
    }



    suspend fun saveUser(userDTO: UserDTO) = Db.getUserDao().insert(userDTO)

    fun getUser() = Db.getUserDao().getuser()

    suspend fun removeUser(userDTO: UserDTO) = Db.getUserDao().removeUser(userDTO)
    fun clearUser() = Db.getUserDao().deleteUser()

}