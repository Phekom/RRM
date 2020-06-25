package za.co.xisystems.itis_rrm.utils.interfaces

import za.co.xisystems.itis_rrm.data.localDB.entities.*
import java.util.*

/**
 * Created by Francis Mahlava on 2019/11/30.
 */
interface IRefreshLookupData {

    fun onSetErrorFlag(isError: Boolean?)
    fun onGetErrorFlag(): Boolean?

    fun onInsertSectionsItems(activitySections: ArrayList<String>)

    fun onInsertContractsProjects(contracts: ArrayList<ContractDTO>)

    fun onInsertWorkFlows(workflows: WorkFlowDTO)

    fun onInsertMobileLookups2(mobileLookups: ArrayList<LookupDTO>)

    fun onInsertProjectItems(items: ArrayList<ProjectItemDTO>)

    fun onInsertProjectSections(projectSections: ArrayList<ProjectSectionDTO>)

    fun onInsertProjectVos(voItems: ArrayList<VoItemDTO>)

    fun insertUserRoles(userRoles: ArrayList<UserRoleDTO>)
}
