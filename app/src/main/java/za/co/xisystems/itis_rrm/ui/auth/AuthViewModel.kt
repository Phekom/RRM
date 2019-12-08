package za.co.xisystems.itis_rrm.ui.auth

import android.view.View
import androidx.lifecycle.ViewModel
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.utils.ApiException
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.NoInternetException
import za.co.xisystems.itis_rrm.utils.lazyDeferred

/**
 * Created by Francis Mahlava on 2019/10/23.
 */


class AuthViewModel(
    private val repository: UserRepository,
    offlineDataRepository: OfflineDataRepository
    ) : ViewModel() {

    var username: String? = null
    var password: String? = null
    var enterPin: String? = null
    var confirmPin: String? = null

    var authListener: AuthListener? = null

    fun getLoggedInUser() = repository.getUser()
    fun getUserRole() = repository.getUser()

    fun onRegButtonClick(view: View) {
        authListener?.onStarted()

        if (username.isNullOrEmpty()) {
            authListener?.onFailure("UserName is required")
            return
        }

        if (password.isNullOrEmpty()) {
            authListener?.onFailure("Password is required")
            return
        }


//        if (confirmPin.isNullOrEmpty()) {
//            authListener?.onFailure("Please Confirm pin")
//            confirmPinEditText?.requestFocus()
//            return
//        }
//
//        if (enterPin != confirmPin) {
//            authListener?.onFailure("Pin did not match")
//            enterPinEditText?.requestFocus()
//            return
//        }




        Coroutines.main {
            try {
                val authResponse = repository.userRegister(username!!, password!!)
                authResponse.user?.let {
                    authListener?.onSuccess(it)
                    repository.saveUser(it)
                    return@main
                }

                authListener?.onFailure(authResponse.errorMessage!!)
            }catch(e: ApiException){
                authListener?.onFailure(e.message!!)
            }catch (e: NoInternetException){
                authListener?.onFailure(e.message!!)
            }
        }

    }

    val user by lazyDeferred {
        repository.getUser()
    }

    val offlinedata by lazyDeferred {
        offlineDataRepository.getSectionItems()
//        offlineDataRepository.getVoItems()
//        offlineDataRepository.getProjects()
//        offlineDataRepository.getWorkFlows()=
        offlineDataRepository.getContracts()

    }


}