package za.co.xisystems.itis_rrm.utils.interfaces

import android.location.Location
import androidx.fragment.app.FragmentActivity

//
// Created by Shaun McDonald on 2020/04/23.
// Copyright (c) 2020 XI Systems. All rights reserved.
//
interface LocationAware {
    fun getHostActivity(): FragmentActivity
    fun getCurrentLocation(): Location?
    fun setCurrentLocation(location: Location?)
}