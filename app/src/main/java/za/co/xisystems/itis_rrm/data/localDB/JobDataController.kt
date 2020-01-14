package za.co.xisystems.itis_rrm.data.localDB

import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository

/**
 * Created by Francis Mahlava on 2020/01/02.
 */
object JobDataController{

    private val offlineDataRepository: OfflineDataRepository? = null

//    suspend fun getRoute(jobId: String?) : String?{
//        if (null == jobId || jobId.length == 0) return null
//        val sectionId = offlineDataRepository?.getProjectSectionIdForJobId(jobId)
//        val route = offlineDataRepository?.getRouteForProjectSectionId(sectionId!!)
//        return getRoute(route)
//    }


//    fun toLittleEndian(bigEndian: String?): String? {
//        if (null == bigEndian || bigEndian.length % 2 != 0) return null
//        val b = bigEndianHexStringToByteArray(bigEndian)
//        return toLittleEndian(b)
//    }

}