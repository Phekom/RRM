package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts

import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO

interface IJobOfflineHelper {
    fun onCreate()
    fun onDestroy()
    fun executeUpdatePhotosLocation(
        job: JobDTO,
        offlineCallBack: OfflineCallBack?
    )

    interface OfflineCallBack {
        fun onCompleted(
            message: String?,
            countSuccess: Int,
            countTotal: Int
        )
    }
}