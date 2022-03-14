package za.co.xisystems.itis_rrm.ui.start

import android.annotation.SuppressLint
import androidx.lifecycle.distinctUntilChanged
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.base.BaseViewModel
import za.co.xisystems.itis_rrm.data.network.responses.HealthCheckResponse
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.utils.lazyDeferred


/**
 * Created by Francis Mahlava on 03,October,2019
 */
@SuppressLint("CustomSplashScreen")
class SplashActivityViewModel(
    private val dataRepository: OfflineDataRepository
) : BaseViewModel() {

    val user by lazyDeferred {
        dataRepository.getUser().distinctUntilChanged()
    }

    suspend fun healthCheck() : Boolean {
        return withContext(Dispatchers.IO) {
            dataRepository.getServiceHealth()
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