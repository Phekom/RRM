package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Contracts(
    @SerializedName("ContractId")
    val contractId: String,
    @SerializedName("ContractNo")
    val contractNo: String,
    @SerializedName("ContractShortCode")
    val contractShortCode: Any,
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("Projects")
    val projects: List<Project>,
    @SerializedName("ShortDescr")
    val shortDescr: String
)