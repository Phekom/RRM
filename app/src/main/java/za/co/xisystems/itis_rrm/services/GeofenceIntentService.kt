package za.co.xisystems.itis_rrm.services

import com.sprotte.geolocator.geofencer.models.Geofence
import com.sprotte.geolocator.geofencer.service.GeofenceIntentService
import timber.log.Timber

//
// Created by Shaun McDonald on 2020/05/25.
// Copyright (c) 2020 XI Systems. All rights reserved.
//
class GeofenceIntentService : GeofenceIntentService() {

    override fun onGeofence(geofence: Geofence) {
        Timber.d("onGeofence $geofence")
    }
}