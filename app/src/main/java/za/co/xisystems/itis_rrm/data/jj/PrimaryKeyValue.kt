package za.co.xisystems.itis_rrm.data.jj


import com.google.gson.annotations.SerializedName

data class PrimaryKeyValue(
    @SerializedName("Key")
    val key: String, // ItemMeasureId
    @SerializedName("Value")
    val value: String, // aKKpCstbSmiZcL3jyWUp2Q==
    @SerializedName("ValueType")
    val valueType: String // System.Byte[]
)