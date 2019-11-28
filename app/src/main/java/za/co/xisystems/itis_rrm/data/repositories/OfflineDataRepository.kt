package za.co.xisystems.itis_rrm.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.SectionItemDTO
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.SafeApiRequest
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.SqlLitUtils
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Pattern

/**
 * Created by Francis Mahlava on 2019/11/28.
 */

const val MINIMUM_INTERVAL = 5

class OfflineDataRepository(
    private val api: BaseConnectionApi,
    private val Db: AppDatabase,
    private val prefs: PreferenceProvider
) : SafeApiRequest() {

    private val conTracts = MutableLiveData<List<ContractDTO>>()
    //    private val sectionItems = MutableLiveData<SectionItemDTO>()
    private val sectionItems = MutableLiveData<ArrayList<String>>()

    init {
        conTracts.observeForever {
            saveContracts(it)
        }
        sectionItems.observeForever {
            //            saveSectionItems(it)
            insertSectionsItems(it)
        }
    }


    suspend fun getContracts(): LiveData<List<ContractDTO>> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getuserID()
            fetchContracts(userId)
            Db.getContractDao().getAllContracts()
        }
    }
    suspend fun getSectionItems(): LiveData<SectionItemDTO> {
        return withContext(Dispatchers.IO) {
            val userId = Db.getUserDao().getuserID()
            fetchContracts(userId)
            Db.getSectionItemDao().getAllSectionItems()
        }
    }
    private fun saveContracts(contracts: List<ContractDTO>) {
        Coroutines.io {
            prefs.savelastSavedAt(LocalDateTime.now().toString())
            Db.getContractDao().saveAllContracts(contracts)
        }
    }
    private fun saveSectionItems(sectionItems: SectionItemDTO) {
        Coroutines.io {
            //            Db.getSectionItemDao().insertEntities(sectionItems)
        }
    }

    private fun insertSectionsItems(activitySections: ArrayList<String>) {
        Coroutines.io {
            prefs.savelastSavedAt(LocalDateTime.now().toString())
            for (activitySection in activitySections) {

                //  Lets get the String
                val pattern = Pattern.compile("(.*?):")
                val matcher = pattern.matcher(activitySection)
                val scetionItemId = SqlLitUtils.generateUuid()

                if (matcher.find()) {
                    Db.getSectionItemDao().insertSectionitem(activitySection,
                        matcher.group(1).replace("\\s+".toRegex(), ""), scetionItemId
                    )

                }

            }
        }

    }















    private suspend fun fetchContracts(userId: String) {
        val lastSavedAt = prefs.getLastSavedAt()
        if (lastSavedAt == null || isFetchNeeded(LocalDateTime.parse(lastSavedAt))) {
            val activitySectionsResponse = apiRequest { api.ActivitySectionsRefresh(userId) }
            sectionItems.postValue(activitySectionsResponse.activitySections)

            val myResponse = apiRequest { api.refreshContractInfo(userId) }
            conTracts.postValue(myResponse.contracts)
        }
    }

    private fun isFetchNeeded(savedAt: LocalDateTime): Boolean {
        return ChronoUnit.MINUTES.between(savedAt, LocalDateTime.now()) > MINIMUM_INTERVAL
    }






}