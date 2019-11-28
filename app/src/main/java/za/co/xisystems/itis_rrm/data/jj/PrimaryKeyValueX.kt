package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName

data class PrimaryKeyValueX(
    @SerializedName("Key")
    val key: String, // JobId
    @SerializedName("Value")
    val value: String, // 8JZmePm6Ru2kNjV2zXmenQ==
    @SerializedName("ValueType")
    val valueType: String // System.Byte[]
)