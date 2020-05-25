package za.co.xisystems.itis_rrm.services

import com.google.android.gms.location.LocationResult
import com.sprotte.geolocator.tracking.service.LocationTrackerUpdateIntentService
import timber.log.Timber

//
// Created by Shaun McDonald on 2020/05/24.
// Copyright (c) 2020 XI Systems. All rights reserved.
//
class LocationTrackerService : LocationTrackerUpdateIntentService() {

    override fun onLocationResult(locationResult: LocationResult) {
        Timber.d("locationResult: $locationResult")
    }
}