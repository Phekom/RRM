/*
 * Updated by Shaun McDonald on 2021/22/20
 * Last modified on 2021/01/20 12:46 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

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

) : Serializable