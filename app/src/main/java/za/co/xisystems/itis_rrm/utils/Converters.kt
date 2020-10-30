@file:Suppress("FunctionName")

package za.co.xisystems.itis_rrm.utils

// import sun.plugin2.util.PojoUtil.toJson
// import jdk.nashorn.internal.objects.NativeDate.getTime
// import sun.plugin2.util.PojoUtil.toJson
import androidx.room.TypeConverter
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import za.co.xisystems.itis_rrm.data.localDB.entities.ActivityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ChildLookupDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.InfoClassDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasurePhotoDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.LookupOptionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.PrimaryKeyValueDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectSectionDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoGroupsDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ToDoListEntityDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.VoItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkFlowRouteDTO

/**
 * Created by Francis Mahlava on 2019/11/22.
 */
val mapper = jacksonObjectMapper()

@Suppress("UNCHECKED_CAST")
class Converters {

    @TypeConverter
    fun restoreList(listOfString: String): ArrayList<String> {
        return Gson().fromJson(listOfString, object : TypeToken<ArrayList<String>>() {
        }.type)
    }

    @TypeConverter
    fun saveList(listOfString: ArrayList<String>): String {
        return Gson().toJson(listOfString)
    }

    @TypeConverter
    fun storedStringToUserRoleDTO(data: String?): ArrayList<UserRoleDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<UserRoleDTO>
        }
        val listType = object : TypeToken<ArrayList<UserRoleDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun userRoleDTOToStoredString(myObjects: ArrayList<UserRoleDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToProjectDTO(data: String?): ArrayList<ProjectDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ProjectDTO>
        }
        val listType = object : TypeToken<ArrayList<ProjectDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun projectDTOToStoredString(myObjects: ArrayList<ProjectDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ======================================================================================
    @TypeConverter
    fun storedStringToWorkFlowRouteDTO(data: String?): ArrayList<WorkFlowRouteDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<WorkFlowRouteDTO>
        }
        val listType = object : TypeToken<ArrayList<WorkFlowRouteDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun workFlowRouteDTOToStoredString(myObjects: ArrayList<WorkFlowRouteDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ======================================================================================
    @TypeConverter
    fun storedStringToJobSectionDTO(data: String?): ArrayList<JobSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobSectionDTO>
        }
        val listType = object : TypeToken<ArrayList<JobSectionDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun jobSectionDTOToStoredString(myObjects: ArrayList<JobSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ======================================================================================
//    @TypeConverter
//    fun storedStringToEntityDTO(data: String?):ArrayList<EntitiesDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST as ArrayList<EntitiesDTO>
//        }
//        val listType = object :TypeToken<ArrayList<EntitiesDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<EntitiesDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun EntityDTOToStoredString(myObjects:ArrayList<EntitiesDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }

    // ======================================================================================
    @TypeConverter
    fun storedStringToItemSectionDTO(data: String?): ArrayList<ItemSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ItemSectionDTO>
        }
        val listType = object : TypeToken<ArrayList<ItemSectionDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun itemSectionDTOToStoredString(myObjects: ArrayList<ItemSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ======================================================================================
    @TypeConverter
    fun storedStringToJobItemEstimateDTO(data: String?): ArrayList<JobItemEstimateDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemEstimateDTO>
        }
        val listType = object : TypeToken<ArrayList<JobItemEstimateDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun jobItemEstimateDTOToStoredString(myObjects: ArrayList<JobItemEstimateDTO>?): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ======================================================================================
    @TypeConverter
    fun storedStringToJobItemMeasureDTO(data: String?): ArrayList<JobItemMeasureDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemMeasureDTO>
        }
        val listType = object : TypeToken<ArrayList<JobItemMeasureDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun jobItemMeasureDTOToStoredString(myObjects: ArrayList<JobItemMeasureDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToToDoListEntityDTO(data: String?): ArrayList<ToDoListEntityDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ToDoListEntityDTO>
        }
        val listType = object : TypeToken<ArrayList<ToDoListEntityDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun toDoListEntityDTOToStoredString(myObjects: ArrayList<ToDoListEntityDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToItemDTO(data: String?): ArrayList<ProjectItemDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ProjectItemDTO>
        }
        val listType = object : TypeToken<ArrayList<ProjectItemDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun ItemDTOToStoredString(myObjects: ArrayList<ProjectItemDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToSectionDTO(data: String?): ArrayList<ProjectSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ProjectSectionDTO>
        }
        val listType = object : TypeToken<ArrayList<ProjectSectionDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun SectionDTOToStoredString(myObjects: ArrayList<ProjectSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToVoItemDTO(data: String?): ArrayList<VoItemDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<VoItemDTO>
        }
        val listType = object : TypeToken<ArrayList<VoItemDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun VoItemDTOToStoredString(myObjects: ArrayList<VoItemDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToChildLookupDTO(data: String?): ArrayList<ChildLookupDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ChildLookupDTO>
        }
        val listType = object : TypeToken<ArrayList<ChildLookupDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun ChildLookupDTOToStoredString(myObjects: ArrayList<ChildLookupDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToLookupOptionDTO(data: String?): ArrayList<LookupOptionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<LookupOptionDTO>
        }
        val listType = object : TypeToken<ArrayList<LookupOptionDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun LookupOptionDTOToStoredString(myObjects: ArrayList<LookupOptionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToPrimaryKeyValueDTO(data: String?): ArrayList<PrimaryKeyValueDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<PrimaryKeyValueDTO>
        }
        val listType = object : TypeToken<ArrayList<PrimaryKeyValueDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun PrimaryKeyValueDTOToStoredString(myObjects: ArrayList<PrimaryKeyValueDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobItemEstimatesPhotoDTO(data: String?): ArrayList<JobItemEstimatesPhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemEstimatesPhotoDTO>
        }
        val listType = object : TypeToken<ArrayList<JobItemEstimatesPhotoDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun JobItemEstimatesPhotoDTOToStoredString(myObjects: ArrayList<JobItemEstimatesPhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobItemEstimatesPhotoDO(data: String?): JobItemEstimatesPhotoDTO? {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as JobItemEstimatesPhotoDTO?
        }
        val listType = object : TypeToken<JobItemEstimatesPhotoDTO?>() {
        }.type
        return gson.fromJson<JobItemEstimatesPhotoDTO?>(data, listType)
    }

    @TypeConverter
    fun JobItemEstimatesPhotoDOToStoredString(myObjects: JobItemEstimatesPhotoDTO?): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================
    @TypeConverter
    fun storedStringToJobEstimateWorksDTO(data: String?): ArrayList<JobEstimateWorksDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobEstimateWorksDTO>
        }
        val listType = object : TypeToken<ArrayList<JobEstimateWorksDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun JobEstimateWorksDTOToStoredString(myObjects: ArrayList<JobEstimateWorksDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobEstimateWorksPhotoDTO(data: String?): ArrayList<JobEstimateWorksPhotoDTO>? {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobEstimateWorksPhotoDTO>?
        }
        val listType = object : TypeToken<ArrayList<JobEstimateWorksPhotoDTO>?>() {
        }.type
        return gson.fromJson<ArrayList<JobEstimateWorksPhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobEstimateWorksPhotoDTOToStoredString(myObjects: ArrayList<JobEstimateWorksPhotoDTO>?): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobDTO(data: String?): ArrayList<JobDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobDTO>
        }
        val listType = object : TypeToken<ArrayList<JobDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun JobDTOToStoredString(myObjects: ArrayList<JobDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobItemMeasurePhotoDTO(data: String?): ArrayList<JobItemMeasurePhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemMeasurePhotoDTO>
        }
        val listType = object : TypeToken<ArrayList<JobItemMeasurePhotoDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun JobItemMeasurePhotoDTOToStoredString(myObjects: ArrayList<JobItemMeasurePhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToActivityDTO(data: String?): ArrayList<ActivityDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ActivityDTO>
        }
        val listType = object : TypeToken<ArrayList<ActivityDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun ActivityDTOToStoredString(myObjects: ArrayList<ActivityDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToInfoClassDTO(data: String?): ArrayList<InfoClassDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<InfoClassDTO>
        }
        val listType = object : TypeToken<ArrayList<InfoClassDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun InfoClassDTOToStoredString(myObjects: ArrayList<InfoClassDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToWorkFlowDTO(data: String?): ArrayList<WorkFlowDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<WorkFlowDTO>
        }
        val listType = object : TypeToken<ArrayList<WorkFlowDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun WorkFlowDTOToStoredString(myObjects: ArrayList<WorkFlowDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToToDoGroupsDTO(data: String?): ArrayList<ToDoGroupsDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ToDoGroupsDTO>
        }
        val listType = object : TypeToken<ArrayList<ToDoGroupsDTO>>() {
        }.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun ToDoGroupsDTOToStoredString(myObjects: ArrayList<ToDoGroupsDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJob(data: String?): JobDTO? {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as JobDTO?
        }
        val listType = object : TypeToken<JobDTO?>() {
        }.type
        return gson.fromJson<JobDTO>(data, listType)
    }

    @TypeConverter
    fun JobToStoredString(myObjects: JobDTO?): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobItemEstimate(data: String?): JobItemEstimateDTO? {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as JobItemEstimateDTO?
        }
        val listType = object : TypeToken<JobItemEstimateDTO>() {
        }.type
        return gson.fromJson<JobItemEstimateDTO>(data, listType)
    }

    @TypeConverter
    fun JobItemEstimateToStoredString(myObjects: JobItemEstimateDTO?): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    // ===========================================================================

    @TypeConverter
    fun storedStringToJobItemMeasure(data: String?): JobItemMeasureDTO? {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as JobItemMeasureDTO?
        }
        val listType = object : TypeToken<JobItemMeasureDTO?>() {
        }.type
        return gson.fromJson<JobItemMeasureDTO>(data, listType)
    }

    @TypeConverter
    fun JobItemMeasureToStoredString(myObjects: JobItemMeasureDTO?): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }
}
