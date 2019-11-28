package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.MutableLiveData
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.HealthDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 2019/11/26.
 */

class HomeRepository (
    private val api : BaseConnectionApi,
    private val Db : AppDatabase
): SafeApiRequest() {

    private val health = MutableLiveData<HealthDTO>()

    init {
        health.observeForever {
            saveHealth(it)
        }
    }

    private fun saveHealth(health: HealthDTO?) {
        Coroutines.io {
//            prefs.savelastSavedAt(LocalDateTime.now().toString())
//            Db.getVoItemDao().insertVoItem(voItems)
        }
    }

//    suspend fun getHealth() : LiveData<HealthDTO>{
//        return withContext(Dispatchers.IO){
//            val userName = Db.getUserDao().getUserName()
//                fetchHealthCheck(userName)
////            Db.getHealthDao().getLife()
//        }
//    }

    suspend fun fetchHealthCheck(userId : String){
        if (isFetchNeeded()){

            val myResponse = apiRequest { api.HealthCheck(userId) }
            health.postValue(myResponse.isAlive)
        }

    }

    private fun isFetchNeeded(): Boolean {
        return true
    }

}

private fun <T> MutableLiveData<T>.postValue(alive: Int) {
    alive.and(1)
}
