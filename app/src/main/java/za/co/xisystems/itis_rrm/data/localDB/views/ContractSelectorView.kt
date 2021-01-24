/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.localDB.views

import androidx.room.DatabaseView

@DatabaseView(
    "SELECT contractId, contractNo, shortDescr FROM CONTRACTS_TABLE ORDER BY contractNo"
)

data class ContractSelectorView(
    val contractId: String,
    val contractNo: String,
    val shortDesc: String
)

