package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models

import java.io.Serializable
import java.util.ArrayList
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO

data class SavedJob(
    var items: ArrayList<ProjectItemDTO>,
    var contract: ContractDTO,
    var project: ProjectDTO,
    var job: JobDTO
) : Serializable
