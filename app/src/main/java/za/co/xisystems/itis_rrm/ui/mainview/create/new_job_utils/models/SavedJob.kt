package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models

import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import java.io.Serializable
import java.util.*

data class SavedJob(var items: ArrayList<ProjectItemDTO>,
                    var contract: ContractDTO,
                    var project: ProjectDTO,
                    var job: JobDTO) : Serializable