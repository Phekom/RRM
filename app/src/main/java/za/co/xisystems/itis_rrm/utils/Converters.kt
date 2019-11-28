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
    fun restoreList(listOfString: String): List<String> {
        return Gson().fromJson(listOfString, object : TypeToken<List<String>>() {

        }.type)
    }

    @TypeConverter
    fun saveList(listOfString: List<String>): String {
        return Gson().toJson(listOfString)
    }


    @TypeConverter
    fun storedStringToUserRoleDTO(data: String?): List<UserRoleDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<UserRoleDTO>>() {

        }.getType()
        return gson.fromJson<List<UserRoleDTO>>(data, listType)
    }

    @TypeConverter
    fun UserRoleDTOToStoredString(myObjects: List<UserRoleDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    //===========================================================================


    @TypeConverter
    fun storedStringToProjectDTO(data: String?): List<ProjectDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<ProjectDTO>>() {

        }.getType()
        return gson.fromJson<List<ProjectDTO>>(data, listType)
    }

    @TypeConverter
    fun ProjectDTOToStoredString(myObjects: List<ProjectDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //======================================================================================
    @TypeConverter
    fun storedStringToWorkFlowRouteDTO(data: String?): List<WorkFlowRouteDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<WorkFlowRouteDTO>>() {

        }.getType()
        return gson.fromJson<List<WorkFlowRouteDTO>>(data, listType)
    }

    @TypeConverter
    fun WorkFlowRouteDTOToStoredString(myObjects: List<WorkFlowRouteDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //======================================================================================
    @TypeConverter
    fun storedStringToJobSectionDTO(data: String?): List<JobSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobSectionDTO>>() {

        }.getType()
        return gson.fromJson<List<JobSectionDTO>>(data, listType)
    }

    @TypeConverter
    fun JobSectionDTOToStoredString(myObjects: List<JobSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //======================================================================================
    @TypeConverter
    fun storedStringToEntityDTO(data: String?): List<EntitiesDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<EntitiesDTO>>() {

        }.getType()
        return gson.fromJson<List<EntitiesDTO>>(data, listType)
    }

    @TypeConverter
    fun EntityDTOToStoredString(myObjects: List<EntitiesDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //======================================================================================
    @TypeConverter
    fun storedStringToItemSectionDTO(data: String?): List<ItemSectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<ItemSectionDTO>>() {

        }.getType()
        return gson.fromJson<List<ItemSectionDTO>>(data, listType)
    }

    @TypeConverter
    fun ItemSectionDTOToStoredString(myObjects: List<ItemSectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //======================================================================================
    @TypeConverter
    fun storedStringToJobItemEstimateDTO(data: String?): List<JobItemEstimateDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobItemEstimateDTO>>() {

        }.getType()
        return gson.fromJson<List<JobItemEstimateDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemEstimateDTOToStoredString(myObjects: List<JobItemEstimateDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //======================================================================================
    @TypeConverter
    fun storedStringToJobItemMeasureDTO(data: String?): List<JobItemMeasureDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobItemMeasureDTO>>() {

        }.getType()
        return gson.fromJson<List<JobItemMeasureDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemMeasureDTOToStoredString(myObjects: List<JobItemMeasureDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //===========================================================================


    @TypeConverter
    fun storedStringToToDoListEntityDTO(data: String?): List<ToDoListEntityDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<ToDoListEntityDTO>>() {

        }.getType()
        return gson.fromJson<List<ToDoListEntityDTO>>(data, listType)
    }

    @TypeConverter
    fun ToDoListEntityDTOToStoredString(myObjects: List<ToDoListEntityDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }




    //===========================================================================


    @TypeConverter
    fun storedStringToItemDTO(data: String?): List<ItemDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<ItemDTO>>() {

        }.getType()
        return gson.fromJson<List<ItemDTO>>(data, listType)
    }

    @TypeConverter
    fun ItemDTOToStoredString(myObjects: List<ItemDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }




    //===========================================================================


    @TypeConverter
    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<SectionDTO>>() {

        }.getType()
        return gson.fromJson<List<SectionDTO>>(data, listType)
    }

    @TypeConverter
    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }

    //===========================================================================


    @TypeConverter
    fun storedStringToVoItemDTO(data: String?): List<VoItemDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<VoItemDTO>>() {

        }.getType()
        return gson.fromJson<List<VoItemDTO>>(data, listType)
    }

    @TypeConverter
    fun VoItemDTOToStoredString(myObjects: List<VoItemDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }



    //===========================================================================


    @TypeConverter
    fun storedStringToChildLookupDTO(data: String?): List<ChildLookupDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<ChildLookupDTO>>() {

        }.getType()
        return gson.fromJson<List<ChildLookupDTO>>(data, listType)
    }

    @TypeConverter
    fun ChildLookupDTOToStoredString(myObjects: List<ChildLookupDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToLookupOptionDTO(data: String?): List<LookupOptionDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<LookupOptionDTO>>() {

        }.getType()
        return gson.fromJson<List<LookupOptionDTO>>(data, listType)
    }

    @TypeConverter
    fun LookupOptionDTOToStoredString(myObjects: List<LookupOptionDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToPrimaryKeyValueDTO(data: String?): List<PrimaryKeyValueDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<PrimaryKeyValueDTO>>() {

        }.getType()
        return gson.fromJson<List<PrimaryKeyValueDTO>>(data, listType)
    }

    @TypeConverter
    fun PrimaryKeyValueDTOToStoredString(myObjects: List<PrimaryKeyValueDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobItemEstimatesPhotoDTO(data: String?): List<JobItemEstimatesPhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobItemEstimatesPhotoDTO>>() {

        }.getType()
        return gson.fromJson<List<JobItemEstimatesPhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemEstimatesPhotoDTOToStoredString(myObjects: List<JobItemEstimatesPhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobEstimateWorksDTO(data: String?): List<JobEstimateWorksDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobEstimateWorksDTO>>() {

        }.getType()
        return gson.fromJson<List<JobEstimateWorksDTO>>(data, listType)
    }

    @TypeConverter
    fun JobEstimateWorksDTOToStoredString(myObjects: List<JobEstimateWorksDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobEstimateWorksPhotoDTO(data: String?): List<JobEstimateWorksPhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobEstimateWorksPhotoDTO>>() {

        }.getType()
        return gson.fromJson<List<JobEstimateWorksPhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobEstimateWorksPhotoDTOToStoredString(myObjects: List<JobEstimateWorksPhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobDTO(data: String?): List<JobDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobDTO>>() {

        }.getType()
        return gson.fromJson<List<JobDTO>>(data, listType)
    }

    @TypeConverter
    fun JobDTOToStoredString(myObjects: List<JobDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


    @TypeConverter
    fun storedStringToJobItemMeasurePhotoDTO(data: String?): List<JobItemMeasurePhotoDTO> {
        val gson = Gson()
        if (data == null) {
            return Collections.emptyList()
        }
        val listType = object : TypeToken<List<JobItemMeasurePhotoDTO>>() {

        }.getType()
        return gson.fromJson<List<JobItemMeasurePhotoDTO>>(data, listType)
    }

    @TypeConverter
    fun JobItemMeasurePhotoDTOToStoredString(myObjects: List<JobItemMeasurePhotoDTO>): String {
        val gson = Gson()
        return gson.toJson(myObjects)
    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }


    //===========================================================================


//    @TypeConverter
//    fun storedStringToSectionDTO(data: String?): List<SectionDTO> {
//        val gson = Gson()
//        if (data == null) {
//            return Collections.emptyList()
//        }
//        val listType = object : TypeToken<List<SectionDTO>>() {
//
//        }.getType()
//        return gson.fromJson<List<SectionDTO>>(data, listType)
//    }
//
//    @TypeConverter
//    fun SectionDTOToStoredString(myObjects: List<SectionDTO>): String {
//        val gson = Gson()
//        return gson.toJson(myObjects)
//    }










































































































































}