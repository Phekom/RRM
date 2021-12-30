package za.co.xisystems.itis_rrm.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.DI
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.ui.mainview.activities.LocationViewModelFactory
import za.co.xisystems.itis_rrm.utils.GPSUtils

/**
 * Created by Shaun McDonald on 2020/06/06.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/
abstract class LocationFragment : BaseFragment() {

    override val di: DI by closestDI()
    private lateinit var locationViewModel: LocationViewModel
    private val locationFactory by instance<LocationViewModelFactory>()
    private var gpsEnabled = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // fragment has no options
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationViewModel =
            ViewModelProvider(this.requireActivity(), locationFactory)[LocationViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GPSUtils(this.requireContext()).activateGPS(object : GPSUtils.OnGpsListener {

            override fun gpsStatus(isGPSEnable: Boolean) {
                this@LocationFragment.gpsEnabled = isGPSEnable
            }
        })
    }

    override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

    protected fun getLocation() = currentLocation

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == GPS_REQUEST) {
            gpsEnabled = true
            invokeLocationAction()
        }
    }

    private fun invokeLocationAction() {
        when {
            !gpsEnabled -> Timber.d("GPS disabled!")

            isPermissionsGranted() -> startLocationUpdate()

            shouldShowRequestPermissionRationale() -> Timber.d("Request Permissions")

            else -> ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_REQUEST
            )
        }
    }

    private fun startLocationUpdate() {
        locationViewModel.getLocationData().observe(this, {
            currentLocation = it
        })
    }

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            this.requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            this.requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST -> {
                invokeLocationAction()
            }
        }
    }

    companion object {
        const val GPS_REQUEST = 100
        const val LOCATION_REQUEST = 101
    }
}
