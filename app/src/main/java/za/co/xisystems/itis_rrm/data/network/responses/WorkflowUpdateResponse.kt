package za.co.xisystems.itis_rrm.data.network.responses

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WorkflowUpdateResponse(
    @SerializedName("ErrorMessage")
    val errorMessage: String?
) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString())

    val isSuccess: Boolean
        get() = errorMessage.isNullOrBlank()

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(errorMessage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WorkflowUpdateResponse> {
        override fun createFromParcel(parcel: Parcel): WorkflowUpdateResponse {
            return WorkflowUpdateResponse(parcel)
        }

        override fun newArray(size: Int): Array<WorkflowUpdateResponse?> {
            return arrayOfNulls(size)
        }
    }
}
