/**
 * Created by Shaun McDonald on 2021/06/02
 * Last modified on 07/01/2021, 13:10
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.auth.model

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(
    private val repository: UserRepository,
    private val xiArmoury: XIArmoury,
    private val photoUtil: PhotoUtil,
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AuthViewModel(repository, xiArmoury, photoUtil, application) as T
    }
}
