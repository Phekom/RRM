package za.co.xisystems.itis_rrm.ui.mainview.estmeasure.submit_measure

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import za.co.xisystems.itis_rrm.data.network.PermissionController
import za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.EstimatePhotoFragment
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.Constants
import za.co.xisystems.itis_rrm.utils.toast

class LocationHelper(private val captureItemMeasurePhotoActivity: CaptureItemMeasurePhotoActivity) : ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {
    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null


    // TODO fix ported legacy code
    @SuppressLint("RestrictedApi")
    fun onCreate() {
        googleApiClient = GoogleApiClient.Builder(captureItemMeasurePhotoActivity)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        locationRequest = LocationRequest()
        locationRequest!!.interval = Constants.UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.fastestInterval = Constants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        locationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // TODO fix ported legacy code
    fun onDestroy() {
        googleApiClient!!.disconnect()
    }

    // TODO fix ported legacy code
    fun onStart() {
        if (PermissionController.checkPermissionsEnabled(getApplicationContext())) {
            googleApiClient!!.connect()
        } else {
            PermissionController.startPermissionRequests(captureItemMeasurePhotoActivity, getApplicationContext())
        }
    }

    // TODO fix ported legacy code
    fun onResume() {
        if (googleApiClient!!.isConnected) {
            startLocationUpdates()
        }
    }

    // TODO fix ported legacy code
    fun onPause() {
        if (googleApiClient!!.isConnected) {
            stopLocationUpdates()
        }
    }

    // TODO fix ported legacy code
    fun onStop() {
        googleApiClient!!.disconnect()
    }


    // TODO fix ported legacy code
    private fun startLocationUpdates() {
        if (PermissionController.checkPermissionsEnabled(getApplicationContext())) {
            if (LocationServices.FusedLocationApi != null) {
                if (ActivityCompat.checkSelfPermission(
                        captureItemMeasurePhotoActivity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        captureItemMeasurePhotoActivity,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) { // TODO: Consider calling
//    ActivityCompat#requestPermissions
// here to request the missing permissions, and then overriding
//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                          int[] grantResults)
// to handle the case where the user grants the permission. See the documentation
// for ActivityCompat#requestPermissions for more details.
                    toast("Error: You need Location permissions!")
                    return
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest,
                    this
                )
            }
        } else {
            PermissionController.startPermissionRequests(captureItemMeasurePhotoActivity)
        }
    }

    // TODO fix ported legacy code
    private fun stopLocationUpdates() {
        if (PermissionController.checkPermissionsEnabled(getApplicationContext())) {
            if (LocationServices.FusedLocationApi != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            }
        } else {
            PermissionController.startPermissionRequests(captureItemMeasurePhotoActivity, getApplicationContext())
        }
    }

    // TODO fix ported legacy code
    private fun getApplicationContext(): Context? {
        return this.captureItemMeasurePhotoActivity.applicationContext
    }

    // TODO fix ported legacy code
    override fun onConnected(connectionHint: Bundle?) {
        if (null == getCurrentLocation()) if (ActivityCompat.checkSelfPermission(
                captureItemMeasurePhotoActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                captureItemMeasurePhotoActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            toast("Error: You need Location permissions!")
            return
        }
        setCurrentLocation(LocationServices.FusedLocationApi.getLastLocation(googleApiClient))
        startLocationUpdates()
    }

    // TODO fix ported legacy code
    override fun onConnectionSuspended(cause: Int) {
        googleApiClient!!.connect()
    }

    // TODO fix ported legacy code
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        toast("Error: Locatin service connection failed!")
    }

    // TODO fix ported legacy code
    private fun toast(s: String) {
        this.toast(s)
    }

    // TODO fix ported legacy code
    override fun onLocationChanged(location: Location?) {
        setCurrentLocation(location)
    }

    fun getCurrentLocation(): Location? {
        return captureItemMeasurePhotoActivity.getCurrentLocation()
    }

    fun setCurrentLocation(location: Location?) {
        captureItemMeasurePhotoActivity.setCurrentLocation(location)
    }

}