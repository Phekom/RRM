package za.co.xisystems.itis_rrm.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import timber.log.Timber

/**
 * Created by Shaun McDonald on 2020/05/31.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */
class LocationProvider private constructor(builder: Builder) :
    LifecycleObserver {
    interface LocationCallback {
        fun onNewLocationAvailable(location: Location?)
        fun locationServicesNotEnabled()
        fun updateLocationInBackground(location: Location?)
        fun networkListenerInitialised()
        fun locationRequestStopped()
    }

    private val callback: LocationCallback?
    private val gpsTimeoutMillis: Long
    private val networkTimeoutMillis: Long
    private val minimumGpsUpdateTime: Int
    private val minimumGpsUpdateDistance: Int
    private val minimumNetworkUpdateTime: Int
    private val minimumNetworkUpdateDistance: Int
    private val loggingEnabled: Boolean
    private val context: Context?

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onLocationResume() {
        requestLocation()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onLocationPause() {
        removeUpdates()
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        if (isRequestingLocation) {
            removeUpdates()
        }
        isFirstToReturn = true
        isRequestingLocation =
            true
        outputLog("starting location service")
        locationManager =
            context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationCallback = callback
        val lastKnownPassiveLocation: Location? =
            getLastKnownLocation(context, LocationManager.PASSIVE_PROVIDER)
        if (lastKnownPassiveLocation != null) {
            outputLog("valid passive provider - callback")
            updateLocation(lastKnownPassiveLocation)
        } else {
            outputLog("invalid passive provider")
            val lastKnownGPS: Location? =
                getLastKnownLocation(context, LocationManager.GPS_PROVIDER)
            if (lastKnownGPS != null) {
                outputLog("invalid passive but valid gps - callback")
                updateLocation(lastKnownGPS)
            } else {
                outputLog("invalid gps provider")
                val lastKnownNetwork: Location? =
                    getLastKnownLocation(context, LocationManager.NETWORK_PROVIDER)
                if (lastKnownNetwork != null) {
                    outputLog("invalid passive and gps but valid network - callback")
                    updateLocation(lastKnownNetwork)
                } else {
                    outputLog("invalid network provider")
                }
            }
        }
        val isGPSEnabled =
            locationManager!!.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )
        val isNetworkEnabled =
            locationManager!!.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        when {
            isGPSEnabled -> {
                gpsTimer =
                    object : CountDownTimer(gpsTimeoutMillis, gpsTimeoutMillis) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            networkUpdatesListener =
                                object : LocationListener {
                                    override fun onLocationChanged(location: Location) {
                                        outputLog("network returned")
                                        updateLocation(location)
                                        removeUpdates()
                                    }

                                    override fun onStatusChanged(
                                        provider: String,
                                        status: Int,
                                        extras: Bundle
                                    ) {
                                    }

                                    override fun onProviderEnabled(provider: String) {}
                                    override fun onProviderDisabled(provider: String) {}
                                }
                            if (isNetworkEnabled) {
                                outputLog("GPS timer finished - Network enabled, listening for updates")
                                startUpdates(
                                    context,
                                    LocationManager.NETWORK_PROVIDER,
                                    minimumNetworkUpdateDistance,
                                    minimumNetworkUpdateTime,
                                    networkUpdatesListener
                                )
                            }
                        }
                    }
                gpsUpdatesListener =
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            outputLog("GPS returned")
                            updateLocation(location)
                            if (gpsTimer != null) {
                                (gpsTimer as CountDownTimer).cancel()
                            }
                            removeUpdates()
                        }

                        override fun onStatusChanged(
                            provider: String,
                            status: Int,
                            extras: Bundle
                        ) {
                        }

                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                outputLog("GPS enabled, listening for updates")
                (gpsTimer as CountDownTimer).start()
                startUpdates(
                    context,
                    LocationManager.GPS_PROVIDER,
                    minimumGpsUpdateTime,
                    minimumGpsUpdateDistance,
                    gpsUpdatesListener
                )
            }
            isNetworkEnabled -> {
                networkUpdatesListener =
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            outputLog("network returned")
                            updateLocation(location)
                            if (networkTimer != null) {
                                networkTimer!!.cancel()
                            }
                            removeUpdates()
                        }

                        override fun onStatusChanged(
                            provider: String,
                            status: Int,
                            extras: Bundle
                        ) {
                        }

                        override fun onProviderEnabled(provider: String) {}
                        override fun onProviderDisabled(provider: String) {}
                    }
                outputLog("Network enabled, listening for updates")
                startUpdates(
                    context,
                    LocationManager.NETWORK_PROVIDER,
                    minimumNetworkUpdateTime,
                    minimumNetworkUpdateDistance,
                    networkUpdatesListener
                )
                networkTimer =
                    object : CountDownTimer(networkTimeoutMillis, networkTimeoutMillis) {
                        override fun onTick(millisUntilFinished: Long) {}
                        override fun onFinish() {
                            outputLog("timer finished")
                            val lastKnownPassiveLocation =
                                getLastKnownLocation(context, LocationManager.PASSIVE_PROVIDER)
                            removeUpdates()
                            if (lastKnownPassiveLocation != null) {
                                outputLog("valid passive provider - callback")
                                updateLocation(lastKnownPassiveLocation)
                            } else {
                                outputLog("timer finished, invalid passive, clearing & restarting")
                                requestLocation()
                            }
                        }
                    }
                (networkTimer as CountDownTimer).start()
            }
            else -> {
                outputLog("No providers are available")
                callback!!.locationServicesNotEnabled()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(
        context: Context?,
        provider: String
    ): Location? {
        if (locationManager == null) {
            locationManager =
                context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        return locationManager!!.getLastKnownLocation(
            provider
        )
    }

    @SuppressLint("MissingPermission")
    private fun startUpdates(
        context: Context?,
        provider: String,
        time: Int,
        distance: Int,
        listener: LocationListener?
    ) {
        if (locationManager == null) {
            locationManager =
                context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        if (provider == LocationManager.NETWORK_PROVIDER) {
            locationCallback!!.networkListenerInitialised()
        }
        locationManager!!.requestLocationUpdates(
            provider,
            time.toLong(),
            distance.toFloat(),
            listener
        )
    }

    private fun updateLocation(location: Location) {
        if (isFirstToReturn) {
            outputLog("first callback")
            locationCallback!!.onNewLocationAvailable(
                location
            )
            isFirstToReturn = false
        } else {
            outputLog("background update")
            locationCallback!!.updateLocationInBackground(
                location
            )
        }
    }

    private fun outputLog(log: String) {
        if (loggingEnabled) {
            Timber.d("$log")
        }
    }

    @SuppressLint("MissingPermission")
    private fun removeUpdates() {
        outputLog("attempting to remove listeners")
        if (gpsUpdatesListener != null && locationManager != null) {
            outputLog("removed gps listener")
            locationManager!!.removeUpdates(
                gpsUpdatesListener
            )
            gpsUpdatesListener = null
        }
        if (networkUpdatesListener != null && locationManager != null) {
            outputLog("removed network listener")
            locationManager!!.removeUpdates(
                networkUpdatesListener
            )
            networkUpdatesListener =
                null
        }
        if (networkTimer != null) {
            outputLog("removed network timer")
            networkTimer!!.cancel()
            networkTimer = null
        }
        if (gpsTimer != null) {
            outputLog("removed GPS timer")
            gpsTimer!!.cancel()
            gpsTimer = null
        }
        if (locationManager != null) {
            outputLog("removed location manager")
            locationManager = null
        }
        isRequestingLocation =
            false
        callback!!.locationRequestStopped()
    }

    class Builder {
        internal var callback: LocationCallback? =
            null
        internal var gpsTimeoutMillis: Long = 7000
        internal var networkTimeoutMillis: Long = 3000
        internal var minimumGpsUpdateTime = 100
        internal var minimumGpsUpdateDistance = 100
        internal var minimumNetworkUpdateTime = 0
        internal var minimumNetworkUpdateDistance = 0
        internal var loggingEnabled = true
        internal var context: Context? = null
        fun setListener(callback: LocationCallback?): Builder {
            this.callback = callback
            return this
        }

        fun setGPSTimeout(gpsTimeoutMillis: Long): Builder {
            this.gpsTimeoutMillis = gpsTimeoutMillis
            return this
        }

        fun setMinimumGpsUpdateTime(minimumGpsUpdateTime: Int): Builder {
            this.minimumGpsUpdateTime = minimumGpsUpdateTime
            return this
        }

        fun setMinimumGpsUpdateDistance(minimumGpsUpdateDistance: Int): Builder {
            this.minimumGpsUpdateDistance = minimumGpsUpdateDistance
            return this
        }

        fun setMinimumNetworkUpdateTime(minimumNetworkUpdateTime: Int): Builder {
            this.minimumNetworkUpdateTime = minimumNetworkUpdateTime
            return this
        }

        fun setMinimumNetworkUpdateDistance(minimumNetworkUpdateDistance: Int): Builder {
            this.minimumNetworkUpdateDistance = minimumNetworkUpdateDistance
            return this
        }

        fun setLoggingEnabled(loggingEnabled: Boolean): Builder {
            this.loggingEnabled = loggingEnabled
            return this
        }

        fun setNetworkTimeout(networkTimeoutMillis: Long): Builder {
            this.networkTimeoutMillis = networkTimeoutMillis
            return this
        }

        fun setContext(context: Context?): Builder {
            this.context = context
            return this
        }

        fun create(): LocationProvider {
            if (context == null) {
                try {
                    throw Exception("Context needs to be passed in")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (callback == null) {
                try {
                    throw Exception("No callback provided, do you expect updates?")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return LocationProvider(this)
        }
    }

    companion object {
        private var locationManager: LocationManager? = null
        private var gpsUpdatesListener: LocationListener? = null
        private var networkUpdatesListener: LocationListener? = null
        private var isFirstToReturn = true
        private var locationCallback: LocationCallback? = null
        private var networkTimer: CountDownTimer? = null
        private var gpsTimer: CountDownTimer? = null
        private var isRequestingLocation = false
    }

    init {
        context = builder.context
        callback = builder.callback
        gpsTimeoutMillis = builder.gpsTimeoutMillis
        networkTimeoutMillis = builder.networkTimeoutMillis
        minimumGpsUpdateTime = builder.minimumGpsUpdateTime
        minimumGpsUpdateDistance = builder.minimumGpsUpdateDistance
        minimumNetworkUpdateDistance = builder.minimumNetworkUpdateDistance
        minimumNetworkUpdateTime = builder.minimumNetworkUpdateTime
        loggingEnabled = builder.loggingEnabled
    }
}
