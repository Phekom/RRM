package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/19.
 */

const val CONTRACTS_TABLE = "CONTRACTS_TABLE"

@Entity(tableName = CONTRACTS_TABLE)

data class ContractDTO(
    @SerializedName("ContractId")
    @PrimaryKey
    val contractId: String,

    @SerializedName("Descr")
    val descr: String?,

    @SerializedName("ShortDescr")
    val shortDescr: String?,

    @SerializedName("ContractNo")
    val contractNo: String?,

    @SerializedName("Projects")
    val projects: ArrayList<ProjectDTO>?

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        TODO("projects")
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contractId)
        parcel.writeString(descr)
        parcel.writeString(shortDescr)
        parcel.writeString(contractNo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContractDTO> {
        override fun createFromParcel(parcel: Parcel): ContractDTO {
            return ContractDTO(parcel)
        }

        override fun newArray(size: Int): Array<ContractDTO?> {
            return arrayOfNulls(size)
        }
    }
}
