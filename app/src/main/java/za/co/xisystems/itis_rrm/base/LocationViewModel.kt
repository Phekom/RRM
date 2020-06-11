package za.co.xisystems.itis_rrm.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import za.co.xisystems.itis_rrm.services.LocationLiveData

/**
 * Created by Shaun McDonald on 2020/06/07.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val locationData = LocationLiveData(application)

    fun getLocationData() = locationData
}
