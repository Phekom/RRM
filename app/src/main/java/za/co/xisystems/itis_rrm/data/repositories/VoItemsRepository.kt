package za.co.xisystems.itis_rrm.data.repositories

import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider

/**
 * Created by Francis Mahlava on 2019/11/19.
 */

private val MINIMUM_INTERVAL1 = 6

class VoItemsRepository(
    private val api : BaseConnectionApi,
    private val Db : AppDatabase,
    private val prefs: PreferenceProvider
): SafeApiRequest() {


//    private val voItems = MutableLiveData<List<VoItemDTO>>()

//    init {
//        voItems.observeForever {
////            saveVoItems(it)
//        }
//    }

//    private fun saveVoItems(voItems: List<VoItemDTO>) {/
//         Coroutines.io {
//             prefs.savelastSavedAt(LocalDateTime.now().toString())
//             Db.getVoItemDao().insertVoItems(voItems)
//         }
//    }

//    suspend fun getVoItems() : LiveData<List<VoItemDTO>>{
//        return withContext(Dispatchers.IO){
//            val ContractVoId = "23EF1CDD14023741AF2F3E698B730B2B"
//            if (existsVo()){
//                Db.getVoItemDao().checkIfVoItemExist(ContractVoId)
//            }

//            fetchVoItems()
//            Db.getVoItemDao().getAllVoltem()
//        }
//    }


//    private suspend fun fetchVoItems(){
//        val lastSavedAt = prefs.getLastSavedAt()
//
//        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))){
////            var userId :String = .toString()
//            val ProjectId = "3a9ddf6c-eb4f-421a-947b-de486ac55875"
//            val myResponse = apiRequest { api.projectVosRefresh(ProjectId) }
//            voItems.postValue(myResponse.voItems)
//        }
//    }

//    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
//        return ChronoUnit.HOURS.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
//    }




}

