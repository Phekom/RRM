/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

/*******************************************************************************
 * Updated by Shaun McDonald on 2021/29/25
 * Last modified on 2021/01/25 3:23 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 ******************************************************************************/

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import org.jetbrains.annotations.NotNull

/**
 * Created by Francis Mahlava on 2019/11/19.
 */

const val CONTRACTS_TABLE = "CONTRACTS_TABLE"

@Entity(tableName = CONTRACTS_TABLE)

data class ContractDTO(
    @SerializedName("ContractId")
    @PrimaryKey
    @NotNull
    val contractId: String,  // E4626638B8B34A34822A2722DC6809B7

    @SerializedName("Descr")
    val descr: String?,

    @SerializedName("ShortDescr")
    val shortDescr: String?,

    @SerializedName("ContractNo")
    val contractNo: String?, // N.002-012-2016/1

    @SerializedName("Projects")
    val projects: ArrayList<ProjectDTO> = ArrayList(),

    @SerializedName("ContractShortCode")
    var contractShortCode: String?,

    @SerializedName("ContractVos")
    var contractVos: ArrayList<ContractVoDTO> = ArrayList(),


    ) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        contractId = parcel.readString()!!,
        descr = parcel.readString(),
        shortDescr = parcel.readString(),
        contractNo = parcel.readString(),
        contractShortCode = parcel.readString(),
        projects = arrayListOf<ProjectDTO>().apply {
            parcel.writeList(this.toList())
        },
        contractVos = arrayListOf<ContractVoDTO>().apply {
            parcel.writeList(this.toList())
        }
    )
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(contractId)
        parcel.writeString(descr)
        parcel.writeString(shortDescr)
        parcel.writeString(contractNo)
        parcel.writeString(contractShortCode)
        parcel.writeList(projects.toList())
        parcel.writeList(contractVos.toList())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<ContractDTO> {
        const val serialVersionUID = 2L
        override fun createFromParcel(parcel: Parcel): ContractDTO {
            return ContractDTO(parcel)
        }

        override fun newArray(size: Int): Array<ContractDTO?> {
            return arrayOfNulls(size)
        }
    }
}
