package za.co.xisystems.itis_rrm.data.localDB.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import za.co.xisystems.itis_rrm.data.localDB.entities.ContractVoDTO
import za.co.xisystems.itis_rrm.domain.ContractVoSelector
import za.co.xisystems.itis_rrm.domain.ProjectVoSelector

/**
 * Created by Francis Mahlava on 2019/11/21.
 */

@Dao
interface ContractVoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertContractVo(contractVo: ContractVoDTO)

    @Query("SELECT EXISTS (SELECT contractId FROM CONTRACTS_VO_TABLE WHERE contractVoId = :contractVoId)")
    fun checkIfContractVoExists(contractVoId: String): Boolean

    @Query("SELECT * FROM CONTRACTS_VO_TABLE WHERE contractId = :contractId")
    fun getContractVoData(contractId: String): List<ContractVoDTO>

    @Query("SELECT contractVoId ,nRAApprovalNumber, voNumber FROM CONTRACTS_VO_TABLE WHERE contractVoId = :contractVoId ORDER BY voNumber")
    fun getContractVoSelectors(contractVoId: String): List<ContractVoSelector>

    @Query("SELECT contractVoId ,nRAApprovalNumber, voNumber FROM CONTRACTS_VO_TABLE WHERE contractId = :contractId ORDER BY voNumber")
    fun getContractVoSelectorsForId(contractId: String): List<ContractVoSelector>








//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun saveAllContracts(contracts: List<ContractDTO>)
//
//




//
//    @Query("SELECT * FROM CONTRACTS_TABLE WHERE contractId = :contractId")
//    fun getContractForContractId(contractId: String): LiveData<ContractDTO>
//
//    @Query("DELETE FROM CONTRACTS_TABLE")
//    fun deleteAll()
//
//    @Query("SELECT contractNo FROM CONTRACTS_TABLE WHERE contractId LIKE :contractVoId")
//    fun getContractNoForId(contractVoId: String?): String
//
//    @Query("SELECT COUNT(contractNo) FROM CONTRACTS_TABLE")
//    fun countContracts(): Int
//
//    @Query("SELECT contractId, contractNo, shortDescr FROM CONTRACTS_TABLE ORDER BY contractNo")
//    fun getContractSelectors(): List<ContractSelector>
}
