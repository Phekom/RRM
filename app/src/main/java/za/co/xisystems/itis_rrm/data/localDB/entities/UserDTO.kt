/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
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
 * Created by Francis Mahlava on 2019/10/23.
 */

const val USER_TABLE = "USER_TABLE"

@Entity(tableName = USER_TABLE)
data class UserDTO(

    @SerializedName("RegistrationId")
    val registrationId: String, // E1676AD5BDEB6C4E8520F48160E01EAC
    @SerializedName("UserId")
    @PrimaryKey
    val userId: String, // 3920
    @SerializedName("UserName")
    val userName: String, // niebuhrk
    @SerializedName("UserRoles")
    val userRoles: ArrayList<UserRoleDTO> = ArrayList(),
    @SerializedName("UserStatus")
    val userStatus: String,
    @SerializedName("PHONE_NUMBER")
    var phoneNumber: String?,
    @SerializedName("IMEI")
    var imei: String?,
    @SerializedName("DEVICE")
    var device: String?,
    var pinHash: String?,
    var authd: Boolean = true

) : Serializable, Parcelable {

    constructor(parcel: Parcel) : this(
        registrationId = parcel.readString()!!,
        userId = parcel.readString()!!,
        userName = parcel.readString()!!,
        userRoles = arrayListOf<UserRoleDTO>().apply {
            parcel.readList(this.toList(), UserRoleDTO::class.java.classLoader)
        },
        userStatus = parcel.readString()!!,
        phoneNumber = parcel.readString(),
        imei = parcel.readString(),
        device = parcel.readString(),
        pinHash = parcel.readString(),
        authd = parcel.readByte() != 0.toByte()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserDTO

        if (registrationId != other.registrationId) return false
        if (userId != other.userId) return false
        if (userName != other.userName) return false
        if (userRoles != other.userRoles) return false
        if (phoneNumber != other.phoneNumber) return false
        if (imei != other.imei) return false
        if (device != other.device) return false

        return true
    }

    override fun hashCode(): Int {
        var result = registrationId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + userRoles.hashCode()
        result = 31 * result + (phoneNumber?.hashCode() ?: 0)
        result = 31 * result + (imei?.hashCode() ?: 0)
        result = 31 * result + (device?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(registrationId)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userStatus)
        parcel.writeString(phoneNumber)
        parcel.writeString(imei)
        parcel.writeString(device)
        parcel.writeString(pinHash)
        parcel.writeList(userRoles.toList())
        parcel.writeByte(if (authd) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<UserDTO> {
        const val serialVersionUID: Long = 25L

        override fun createFromParcel(parcel: Parcel): UserDTO {
            return UserDTO(parcel)
        }

        override fun newArray(size: Int): Array<UserDTO?> {
            return arrayOfNulls(size)
        }
    }
}
