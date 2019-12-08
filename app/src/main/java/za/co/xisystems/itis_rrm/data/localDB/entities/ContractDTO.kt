package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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


)
