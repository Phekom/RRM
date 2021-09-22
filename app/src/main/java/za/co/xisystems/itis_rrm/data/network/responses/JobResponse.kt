package za.co.xisystems.itis_rrm.data.network.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.WorkflowJobDTO

data class JobResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String?,
    @SerializedName("Job")
    val job: JobDTO,
//    val job: ArrayList<JobDTO>
//    @SerializedName("ToDoListGroups")
    @SerializedName("WorkflowJob")
    var workflowJob: WorkflowJobDTO?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readParcelable(JobDTO::class.java.classLoader)!!,
        parcel.readParcelable(WorkflowJobDTO::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(errorMessage)
        parcel.writeParcelable(job, flags)
        parcel.writeParcelable(workflowJob, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<JobResponse> {
        override fun createFromParcel(parcel: Parcel): JobResponse {
            return JobResponse(parcel)
        }

        override fun newArray(size: Int): Array<JobResponse?> {
            return arrayOfNulls(size)
        }
    }
}
