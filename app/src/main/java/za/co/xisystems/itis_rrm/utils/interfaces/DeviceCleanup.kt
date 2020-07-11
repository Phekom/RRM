package za.co.xisystems.itis_rrm.utils.interfaces

import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import java.util.ArrayList

/**
 * Created by Francis Mahlava on 2019/11/30.
 */
interface DeviceCleanup {
    fun fullDeleteJob()

    fun cleanupWithJob(selectedItem: ProjectItemDTO, jobItemEstimateSelect: JobItemEstimateDTO)

    fun cleanupWithNoJob(
        selectedItem: ProjectItemDTO,
        jobItemsEstimate: ArrayList<JobItemEstimateDTO>
    )
}
