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
    fun saveAllContracts(contracts: List<ContractDTO>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: ContractDTO)

    @Query("SELECT * FROM CONTRACTS_TABLE ORDER BY contractNo")
    fun getAllContracts(): LiveData<List<ContractDTO>>

    @Query("SELECT EXISTS (SELECT contractId FROM CONTRACTS_TABLE WHERE contractId = :contractId)")
    fun checkIfContractExists(contractId: String): Boolean

    @Query("SELECT * FROM CONTRACTS_TABLE WHERE contractId = :contractId")
    fun getContractForContractId(contractId: String): LiveData<ContractDTO>

    @Query("DELETE FROM CONTRACTS_TABLE")
    fun deleteAll()

    @Query("SELECT contractNo FROM CONTRACTS_TABLE WHERE contractId LIKE :contractVoId")
    fun getContractNoForId(contractVoId: String?): String

    @Query("SELECT COUNT(contractNo) FROM CONTRACTS_TABLE")
    fun countContracts(): Int
}
