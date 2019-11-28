package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class PrjJobDto(
    @SerializedName("ActId")
    val actId: Int,
    @SerializedName("ApprovalDate")
    val approvalDate: Any,
    @SerializedName("ContractVoId")
    val contractVoId: String,
    @SerializedName("ContractorId")
    val contractorId: Int,
    @SerializedName("Cpa")
    val cpa: Int,
    @SerializedName("DayWork")
    val dayWork: Int,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("DueDate")
    val dueDate: String,
    @SerializedName("EndKm")
    val endKm: Double,
    @SerializedName("EngineerId")
    val engineerId: Int,
    @SerializedName("EntireRoute")
    val entireRoute: Int,
    @SerializedName("IsExtraWork")
    val isExtraWork: Int,
    @SerializedName("IssueDate")
    val issueDate: String,
    @SerializedName("JiNo")
    val jiNo: String,
    @SerializedName("JobCategoryId")
    val jobCategoryId: Int,
    @SerializedName("JobDirectionId")
    val jobDirectionId: Int,
    @SerializedName("JobId")
    val jobId: String,
    @SerializedName("JobPositionId")
    val jobPositionId: Int,
    @SerializedName("JobStatusId")
    val jobStatusId: Int,
    @SerializedName("M9100")
    val m9100: Int,
    @SerializedName("MobileJobItemEstimates")
    val mobileJobItemEstimates: Any,
    @SerializedName("MobileJobItemMeasures")
    val mobileJobItemMeasures: Any,
    @SerializedName("MobileJobSections")
    val mobileJobSections: Any,
    @SerializedName("PerfitemGroupId")
    val perfitemGroupId: Any,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("ProjectVoId")
    val projectVoId: Any,
    @SerializedName("QtyUpdateAllowed")
    val qtyUpdateAllowed: Int,
    @SerializedName("RecordSynchStateId")
    val recordSynchStateId: Int,
    @SerializedName("RecordVersion")
    val recordVersion: Int,
    @SerializedName("Remarks")
    val remarks: Any,
    @SerializedName("Route")
    val route: Any,
    @SerializedName("RrmJiNo")
    val rrmJiNo: Any,
    @SerializedName("Section")
    val section: Any,
    @SerializedName("StartDate")
    val startDate: String,
    @SerializedName("StartKm")
    val startKm: Double,
    @SerializedName("TrackRouteId")
    val trackRouteId: String,
    @SerializedName("UserId")
    val userId: Int,
    @SerializedName("VoId")
    val voId: Any,
    @SerializedName("WorkCompleteDate")
    val workCompleteDate: Any,
    @SerializedName("WorkStartDate")
    val workStartDate: Any
)