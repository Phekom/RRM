package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils

import android.Manifest
import android.app.Activity
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
import za.co.xisystems.itis_rrm.data.network.PermissionController.checkPermissionsEnabled
import za.co.xisystems.itis_rrm.data.network.PermissionController.startPermissionRequests

class LocationHelper(private val activity: Activity) : ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {
    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null
    // TODO fix ported legacy code
    fun onCreate() {
        googleApiClient = GoogleApiClient.Builder(activity)
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
        if (checkPermissionsEnabled(applicationContext)) {
            googleApiClient!!.connect()
        } else {
            startPermissionRequests(activity, applicationContext)
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
        if (checkPermissionsEnabled(applicationContext)) {
            if (LocationServices.FusedLocationApi != null) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        activity,
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
            startPermissionRequests(activity)
        }
    }

    // TODO fix ported legacy code
    private fun stopLocationUpdates() {
        if (checkPermissionsEnabled(applicationContext)) {
            if (LocationServices.FusedLocationApi != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
            }
        } else {
            startPermissionRequests(activity, applicationContext)
        }
    }

    // TODO fix ported legacy code
    private val applicationContext: Context
        private get() = activity.applicationContext

    // TODO fix ported legacy code
    override fun onConnected(connectionHint: Bundle?) {
        if (null == currentLocation) if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                activity,
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
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
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
        toast(s)
    }

    // TODO fix ported legacy code
    override fun onLocationChanged(location: Location) {
        currentLocation = location
    }

    var currentLocation: Location?


        get() = currentLocation
        set(location) {
//            activity.setCurrentLocation(location)
        }

}