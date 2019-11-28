package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractDTO

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ContractDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAllContracts(contracts : List<ContractDTO> )


    @Query("SELECT * FROM CONTRACTS_TABLE")
    fun getAllContracts() : LiveData<List<ContractDTO>>

    @Query("SELECT * FROM CONTRACTS_TABLE WHERE contractId = :contractId")
    fun checkIfContractExists(contractId: String): LiveData<ContractDTO>

    @Query("SELECT * FROM CONTRACTS_TABLE WHERE contractId = :contractId")
    fun getContractForContractId(contractId: String): LiveData<ContractDTO>

    @Query("DELETE FROM CONTRACTS_TABLE")
    fun deleteAll()





//    //  Get All Contracts for Versio
//    @Query("SELECT * FROM CONTRACTS_TABLE ")
//    fun getAllContracts() : LiveData<List<Contract>>
//

////    fun checkIfContractExists(contractId: String): LiveData<Boolean>
//

//


}