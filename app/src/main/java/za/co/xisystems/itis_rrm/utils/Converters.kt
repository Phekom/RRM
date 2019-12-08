package za.co.xisystems.itis_rrm.utils

//import sun.plugin2.util.PojoUtil.toJson
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.xisystems.itis_rrm.data.localDB.entities.*
import java.util.*





/**
 * Created by Francis Mahlava on 2019/11/22.
 */

class Converters {

    @TypeConverter
    fun restoreList(listOfString: String):ArrayList<String> {
        return Gson().fromJson(listOfString, object : TypeToken<ArrayList<String>>() {

        }.type)
    }

    @TypeConverter
    fun saveList(listOfString :  ArrayList<String>): String {
        return Gson().toJson(listOfString)
    }


    @TypeConverter
    fun storedStringToUserRoleDTO(data: String?) : ArrayList<UserRoleDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<UserRoleDTO>
        }
        val listType = object :TypeToken<ArrayList<UserRoleDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<UserRoleDTO>>(data, listType)
    }

    @TypeConverter
    fun UserRoleDTOToStoredString(myObjects: ArrayList<UserRoleDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    //===========================================================================


    @TypeConverter
    fun storedStringToProjectDTO(data: String?): ArrayList<ProjectDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ProjectDTO>
        }
        val listType = object : TypeToken<ArrayList<ProjectDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ProjectDTO>>(data, listType)
    }

    @TypeConverter
    fun ProjectDTOToStoredString(myObjects: ArrayList<ProjectDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //======================================================================================
    @TypeConverter
    fun storedStringToWorkFlowRouteDTO(data: String?): ArrayList<WorkFlowRouteDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<WorkFlowRouteDTO>
        }
        val listType = object :TypeToken<ArrayList<WorkFlowRouteDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<WorkFlowRouteDTO>>(data, listType)
    }

    @TypeConverter
    fun WorkFlowRouteDTOToStoredString(myObjects: ArrayList<WorkFlowRouteDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //======================================================================================
    @TypeConverter
    fun storedStringToJobSectionDTO(data: String?):ArrayList<JobSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobSectionDTO>
        }
        val listType = object :TypeToken<ArrayList<JobSectionDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobSectionDTO>>(data, listType)
    }

    @TypeConverter
    fun JobSectionDTOToStoredString(myObjects:ArrayList<JobSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //======================================================================================
    @TypeConverter
    fun storedStringToEntityDTO(data: String?):ArrayList<EntitiesDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<EntitiesDTO>
        }
        val listType = object :TypeToken<ArrayList<EntitiesDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<EntitiesDTO>>(data, listType)
    }

    @TypeConverter
    fun EntityDTOToStoredString(myObjects:ArrayList<EntitiesDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //======================================================================================
    @TypeConverter
    fun storedStringToItemSectionDTO(data: String?):ArrayList<ItemSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ItemSectionDTO>
        }
        val listType = object :TypeToken<ArrayList<ItemSectionDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ItemSectionDTO>>(data, listType)
    }

    @TypeConverter
    fun ItemSectionDTOToStoredString(myObjects:ArrayList<ItemSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //======================================================================================
    @TypeConverter
    fun storedStringToJobItemEstimateDTO(data: String?):ArrayList<JobItemEstimateDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemEstimateDTO>
        }
        val listType = object :TypeToken<ArrayList<JobItemEstimateDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobItemEstimateDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemEstimateDTOToStoredString(myObjects:ArrayList<JobItemEstimateDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //======================================================================================
    @TypeConverter
    fun storedStringToJobItemMeasureDTO(data: String?):ArrayList<JobItemMeasureDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemMeasureDTO>
        }
        val listType = object :TypeToken<ArrayList<JobItemMeasureDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobItemMeasureDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemMeasureDTOToStoredString(myObjects:ArrayList<JobItemMeasureDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //===========================================================================


    @TypeConverter
    fun storedStringToToDoListEntityDTO(data: String?):ArrayList<ToDoListEntityDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ToDoListEntityDTO>
        }
        val listType = object :TypeToken<ArrayList<ToDoListEntityDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ToDoListEntityDTO>>(data, listType)
    }

    @TypeConverter
    fun ToDoListEntityDTOToStoredString(myObjects:ArrayList<ToDoListEntityDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }




    //===========================================================================


    @TypeConverter
    fun storedStringToItemDTO(data: String?):ArrayList<ItemDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ItemDTO>
        }
        val listType = object :TypeToken<ArrayList<ItemDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ItemDTO>>(data, listType)
    }

    @TypeConverter
    fun ItemDTOToStoredString(myObjects:ArrayList<ItemDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }




    //===========================================================================


    @TypeConverter
    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ProjectSectionDTO>
        }
        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
    }

    @TypeConverter
    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    //===========================================================================


    @TypeConverter
    fun storedStringToVoItemDTO(data: String?):ArrayList<VoItemDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<VoItemDTO>
        }
        val listType = object :TypeToken<ArrayList<VoItemDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<VoItemDTO>>(data, listType)
    }

    @TypeConverter
    fun VoItemDTOToStoredString(myObjects:ArrayList<VoItemDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //===========================================================================


    @TypeConverter
    fun storedStringToChildLookupDTO(data: String?):ArrayList<ChildLookupDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ChildLookupDTO>
        }
        val listType = object :TypeToken<ArrayList<ChildLookupDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ChildLookupDTO>>(data, listType)
    }

    @TypeConverter
    fun ChildLookupDTOToStoredString(myObjects:ArrayList<ChildLookupDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToLookupOptionDTO(data: String?):ArrayList<LookupOptionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<LookupOptionDTO>
        }
        val listType = object :TypeToken<ArrayList<LookupOptionDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<LookupOptionDTO>>(data, listType)
    }

    @TypeConverter
    fun LookupOptionDTOToStoredString(myObjects:ArrayList<LookupOptionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToPrimaryKeyValueDTO(data: String?):ArrayList<PrimaryKeyValueDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<PrimaryKeyValueDTO>
        }
        val listType = object :TypeToken<ArrayList<PrimaryKeyValueDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<PrimaryKeyValueDTO>>(data, listType)
    }

    @TypeConverter
    fun PrimaryKeyValueDTOToStoredString(myObjects:ArrayList<PrimaryKeyValueDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobItemEstimatesPhotoDTO(data: String?):ArrayList<JobItemEstimatesPhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemEstimatesPhotoDTO>
        }
        val listType = object :TypeToken<ArrayList<JobItemEstimatesPhotoDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobItemEstimatesPhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemEstimatesPhotoDTOToStoredString(myObjects:ArrayList<JobItemEstimatesPhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobEstimateWorksDTO(data: String?):ArrayList<JobEstimateWorksDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobEstimateWorksDTO>
        }
        val listType = object :TypeToken<ArrayList<JobEstimateWorksDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobEstimateWorksDTO>>(data, listType)
    }

    @TypeConverter
    fun JobEstimateWorksDTOToStoredString(myObjects:ArrayList<JobEstimateWorksDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobEstimateWorksPhotoDTO(data: String?):ArrayList<JobEstimateWorksPhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobEstimateWorksPhotoDTO>
        }
        val listType = object :TypeToken<ArrayList<JobEstimateWorksPhotoDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobEstimateWorksPhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobEstimateWorksPhotoDTOToStoredString(myObjects:ArrayList<JobEstimateWorksPhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobDTO(data: String?): ArrayList<JobDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobDTO>
        }
        val listType = object :TypeToken<ArrayList<JobDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobDTO>>(data, listType)
    }

    @TypeConverter
    fun JobDTOToStoredString(myObjects:ArrayList<JobDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobItemMeasurePhotoDTO(data: String?):ArrayList<JobItemMeasurePhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<JobItemMeasurePhotoDTO>
        }
        val listType = object :TypeToken<ArrayList<JobItemMeasurePhotoDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<JobItemMeasurePhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemMeasurePhotoDTOToStoredString(myObjects:ArrayList<JobItemMeasurePhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToActivityDTO(data: String?):ArrayList<ActivityDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ActivityDTO>
        }
        val listType = object :TypeToken<ArrayList<ActivityDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ActivityDTO>>(data, listType)
    }

    @TypeConverter
    fun ActivityDTOToStoredString(myObjects:ArrayList<ActivityDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToInfoClassDTO(data: String?):ArrayList<InfoClassDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<InfoClassDTO>
        }
        val listType = object :TypeToken<ArrayList<InfoClassDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<InfoClassDTO>>(data, listType)
    }

    @TypeConverter
    fun InfoClassDTOToStoredString(myObjects:ArrayList<InfoClassDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToWorkFlowDTO(data: String?):ArrayList<WorkFlowDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<WorkFlowDTO>
        }
        val listType = object :TypeToken<ArrayList<WorkFlowDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<WorkFlowDTO>>(data, listType)
    }

    @TypeConverter
    fun WorkFlowDTOToStoredString(myObjects:ArrayList<WorkFlowDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToToDoGroupsDTO(data: String?):ArrayList<ToDoGroupsDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.EMPTY_LIST as ArrayList<ToDoGroupsDTO>
        }
        val listType = object :TypeToken<ArrayList<ToDoGroupsDTO>>() {

        }.getType()
        return gson.fromJson<ArrayList<ToDoGroupsDTO>>(data, listType)
    }

    @TypeConverter
    fun ToDoGroupsDTOToStoredString(myObjects:ArrayList<ToDoGroupsDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object :TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects:ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?):ArrayList<ProjectSectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.EMPTY_LIST
//        }
//        val listType = object : TypeToken<ArrayList<ProjectSectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<ArrayList<ProjectSectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: ArrayList<ProjectSectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }










































































































































}