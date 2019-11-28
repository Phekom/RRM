package za.co.xisystems.itis_rrm.ui.mainview.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.ContractsRepository

/**
 * Created by Francis Mahlava on 2019/10/18.
 */

@Suppress("UNCHECKED_CAST")
class CreateViewModelFactory(
    private val contractsRepository: ContractsRepository

): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CreateViewModel(contractsRepository) as T
    }
}