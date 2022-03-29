package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2022/03/21.
 */

const val CONTRACTS_VO_TABLE = "CONTRACTS_VO_TABLE"

@Entity(tableName = CONTRACTS_VO_TABLE)

data class ContractVoDTO(
    @SerializedName("ContractId")
    var contractId: String?, // E4626638B8B34A34822A2722DC6809B7
    @SerializedName("ContractVoId")
    @PrimaryKey
    var contractVoId: String, // 46D0876478DBB34BB5A3B562A29FCC74
    @SerializedName("EndDate")
    var endDate: String?, // 2019-09-30T23:59:59
    @SerializedName("NRAApprovalDate")
    var nRAApprovalDate: String?, // 2016-12-13T00:00:00
    @SerializedName("NRAApprovalNumber")
    var nRAApprovalNumber: String?, // W1614N0020120161ANDR30001020161
    @SerializedName("StartDate")
    var startDate: String?, // 2017-05-19T00:00:00
    @SerializedName("VoNumber")
    var voNumber: String?, // WA2009
    @SerializedName("VoValue")
    var voValue: Double? // 15348.97

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contractId)
        parcel.writeString(contractVoId)
        parcel.writeString(endDate)
        parcel.writeString(nRAApprovalDate)
        parcel.writeString(nRAApprovalNumber)
        parcel.writeString(startDate)
        parcel.writeString(voNumber)
        parcel.writeDouble(voValue!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContractVoDTO> {
        override fun createFromParcel(parcel: Parcel): ContractVoDTO {
            return ContractVoDTO(parcel)
        }

        override fun newArray(size: Int): Array<ContractVoDTO?> {
            return arrayOfNulls(size)
        }
    }
}