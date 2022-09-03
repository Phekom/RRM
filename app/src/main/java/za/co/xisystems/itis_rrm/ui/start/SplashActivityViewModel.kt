package za.co.xisystems.itis_rrm.ui.start

import android.annotation.SuppressLint
import android.os.Build
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.password4j.SecureString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.data.network.responses.AuthResponse
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.data.preferences.MyAppPrefsManager
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.utils.lazyDeferred


/**
 * Created by Francis Mahlava on 03,October,2019
 */
@SuppressLint("CustomSplashScreen")
class SplashActivityViewModel(
    private val dataRepository: OfflineDataRepository,
    private val armoury: XIArmoury,
) : BaseViewModel() {

    val user by lazyDeferred {
        dataRepository.getUser().distinctUntilChanged()
    }

    suspend fun healthCheck() : Boolean {
        return withContext(Dispatchers.IO) {
            dataRepository.getServiceHealth()
        }
    }

    suspend fun checkUser(userName: String, myAppPrefsManager: MyAppPrefsManager?, ): String {
        var errorMessage = ""
        val phoneNumber = "12345457"
        val imie = "123-imei-45678"
        val androidDevice =
            "${R.string.android_sdk} ${Build.VERSION.SDK_INT} " +
                    "${Build.BRAND} ${Build.MODEL} ${Build.DEVICE}"

        val inputHash = SecureString(
            userName.plus(androidDevice).plus(myAppPrefsManager?.getSigned()!!).toCharArray(),
            false
        )
        val result = armoury.validateToken(
            inputHash, myAppPrefsManager.getAccessToken()!!
        )
        if (!result) {
            errorMessage =  "Your Password May Have Changed Please Contact ITIS Support"
        }else{
            val authResponse =  dataRepository.checkUser( userName.trim(), myAppPrefsManager.getSigned()!!, phoneNumber, imie, androidDevice)
            errorMessage = authResponse.user?.userStatus ?: ""
        }
        return withContext(Dispatchers.IO) {
            errorMessage
        }
    }



    suspend fun getAppVersionCheck(versionNmb: String) : VersionCheckResponse {
        val response = dataRepository.getAppVersionCheck(versionNmb)
        return withContext(ioContext) {
            response
        }
    }

    suspend fun getHealthCheck() : HealthCheckResponse {
        return withContext(Dispatchers.IO) {
            dataRepository.getHealthCheck()
        }
    }

}