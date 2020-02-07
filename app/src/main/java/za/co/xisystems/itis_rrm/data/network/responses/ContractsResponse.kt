package za.co.xisystems.itis_rrm.data.network.responses


import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO

data class ContractsResponse(
    @SerializedName("Contracts")
    val contracts: List<ContractDTO>,

    @SerializedName("ErrorMessage")
    val errorMessage: String
)