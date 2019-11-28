package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class PrimaryKeyValue(
    @SerializedName("Key")
    val key: String,
    @SerializedName("Value")
    val value: String,
    @SerializedName("ValueType")
    val valueType: String
)