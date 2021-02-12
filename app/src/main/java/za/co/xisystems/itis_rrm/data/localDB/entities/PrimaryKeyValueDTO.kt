/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

// import com.google.android.gms.common.util.Base64Utils
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.springframework.util.Base64Utils

/**
 * Created by Francis Mahlava on 2019/11/22.
 */

const val PRIMARY_KEY_VALUE_TABLE = "PRIMARY_KEY_VALUE_TABLE"

@Entity(tableName = PRIMARY_KEY_VALUE_TABLE)
data class PrimaryKeyValueDTO(
    @PrimaryKey
    val id: Int,
    @SerializedName("Key")
    val primary_key: String?,
    @SerializedName("Value")
    var valueString: String?,

    var trackRouteId: String?,

    @SerializedName("ActivityId")
    val activityId: Int, // 3

    var valueBytes: ByteArray?,

    @SerializedName("ValueType")
    val valueType: String?

) : Parcelable {
    var pValue: ByteArray?
        get() = if (valueString == null) valueBytes else Base64Utils.decodeFromString(valueString)
        set(value) {
            this.valueBytes = value
            this.valueString = Base64Utils.encode(value).toString()
        }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.createByteArray(),
        parcel.readString()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrimaryKeyValueDTO

        if (id != other.id) return false
        if (valueBytes != null) {
            if (other.valueBytes == null) return false
            if (!valueBytes!!.contentEquals(other.valueBytes!!)) return false
        } else if (other.valueBytes != null) return false
        if (valueType != other.valueType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (valueBytes?.contentHashCode() ?: 0)
        result = 31 * result + (valueType?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(primary_key)
        parcel.writeString(valueString)
        parcel.writeString(trackRouteId)
        parcel.writeInt(activityId)
        parcel.writeByteArray(valueBytes)
        parcel.writeString(valueType)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Creator<PrimaryKeyValueDTO> {
        override fun createFromParcel(parcel: Parcel): PrimaryKeyValueDTO {
            return PrimaryKeyValueDTO(parcel)
        }

        override fun newArray(size: Int): Array<PrimaryKeyValueDTO?> {
            return arrayOfNulls(size)
        }
    }
}
