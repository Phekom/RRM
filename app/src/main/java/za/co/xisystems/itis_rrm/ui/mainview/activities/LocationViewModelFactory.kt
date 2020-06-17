package za.co.xisystems.itis_rrm.ui.mainview.activities

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.xisystems.itis_rrm.base.LocationViewModel

/**
 * Created by Shaun McDonald on 2020/06/07.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/

@Suppress("UNCHECKED_CAST")
class LocationViewModelFactory(private val application: Application) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LocationViewModel(application) as T
    }
}
