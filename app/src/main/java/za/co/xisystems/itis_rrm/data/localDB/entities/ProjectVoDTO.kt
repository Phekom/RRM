package za.co.xisystems.itis_rrm.data.localDB.entities

//
//import android.os.Parcel
//import android.os.Parcelable
//import androidx.room.Entity
//import androidx.room.PrimaryKey
//import com.google.gson.annotations.SerializedName
//import org.jetbrains.annotations.NotNull
//import org.jetbrains.annotations.Nullable
//import java.io.Serializable
//
///**
// * Created by Francis Mahlava on 2022/03/21.
// */
//
//const val PROJECTS_VO_TABLE = "PROJECTS_VO_TABLE"
//
//@Entity(tableName = PROJECTS_VO_TABLE)
//
//data class ProjectVoDTO(
//    @SerializedName("ItemId")
//    var itemId: String?,
//    @SerializedName("ContractVoId")
//    var contractVoId: String?, // 46D0876478DBB34BB5A3B562A29FCC74
//    @SerializedName("Descr")
//    var descr: String?, // CONTRACTORS MARKUP WA2009
//    @SerializedName("ItemCode")
//    var itemCode: String?,
//    @SerializedName("ProjectId")
//    var projectId: String?, // 2A74D6A6D69842D1A2ADFD54BF7308CD
//    @SerializedName("ProjectVoId")
//    @PrimaryKey
//    var projectVoId: String, // B6DBD0E04A50FD4BB3DE196C2AFE0337
//    @SerializedName("TenderValue")
//    var tenderValue: Double?, // 1.0
//    @SerializedName("NRAApprovalNumber")
//    var approvalNumber: String?, // W1614N0020120161ANDR30001020161
//    @SerializedName("VoNumber")
//    var voNumber: String?, // WA2009
//    @SerializedName("Uom")
//    val uom: String?,
//    @SerializedName("WorkflowId")
//    var workflowId: Int = 0,
//    @Nullable
//    @SerializedName("ItemSections")
//    val itemSections: ArrayList<ItemSectionDTO> = ArrayList(),
//    var sectionItemId: String?,
//    val quantity: Double = 0.toDouble(),
//    val estimateId: String?,
//
//
//    ) : Serializable, Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString()!!,
//        parcel.readDouble(),
//        parcel.readString()!!,
//        parcel.readString()!!,
//        parcel.readString(),
//        workflowId = parcel.readInt(),
//        sectionItemId = parcel.readString(),
//        quantity = parcel.readDouble(),
//        estimateId = parcel.readString(),
//        itemSections = arrayListOf<ItemSectionDTO>().apply {
//            parcel.readList(this.toList(), ItemSectionDTO::class.java.classLoader)
//        },
//    ) {
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(itemId)
//        parcel.writeString(contractVoId)
//        parcel.writeString(descr)
//        parcel.writeString(itemCode)
//        parcel.writeString(projectId)
//        parcel.writeString(projectVoId)
//        parcel.writeDouble(tenderValue!!)
//        parcel.writeString(approvalNumber)
//        parcel.writeString(voNumber)
//        parcel.writeString(uom)
//        parcel.writeValue(workflowId)
//        parcel.writeString(sectionItemId)
//        parcel.writeDouble(quantity)
//        parcel.writeString(estimateId)
//        parcel.writeList(itemSections.toList())
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<ProjectVoDTO> {
//        override fun createFromParcel(parcel: Parcel): ProjectVoDTO {
//            return ProjectVoDTO(parcel)
//        }
//
//        override fun newArray(size: Int): Array<ProjectVoDTO?> {
//            return arrayOfNulls(size)
//        }
//    }
//}