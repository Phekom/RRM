/*
 * Updated by Shaun McDonald on 2021/02/08
 * Last modified on 2021/02/08 2:32 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

const val USER_ROLE_TABLE = "USER_ROLE_TABLE"

@Entity(tableName = USER_ROLE_TABLE)
data class UserRoleDTO(

    @SerializedName("RoleIdentifier")
    @PrimaryKey
    val roleIdentifier: String, // D9E16C2A31FA4CC28961E20B652B292C
    @SerializedName("RoleDescription")
    val roleDescription: String // RRM Job Mobile - Engineer

) : Serializable, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(roleIdentifier)
        parcel.writeString(roleDescription)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<UserRoleDTO> {
        override fun createFromParcel(parcel: Parcel): UserRoleDTO {
            return UserRoleDTO(parcel)
        }

        override fun newArray(size: Int): Array<UserRoleDTO?> {
            return arrayOfNulls(size)
        }
    }
}
