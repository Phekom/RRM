package za.co.xisystems.itis_rrm.base

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants.DNS_PORT
import za.co.xisystems.itis_rrm.constants.Constants.FIVE_SECONDS
import za.co.xisystems.itis_rrm.constants.Constants.SSL_PORT
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration.LONG
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.Animations
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView
import za.co.xisystems.itis_rrm.forge.DefaultDispatcherProvider
import za.co.xisystems.itis_rrm.forge.DispatcherProvider
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.services.LocationModel
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.LocationViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.utils.GPSUtils
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.ViewLogger

/**
 * Created by Francis Mahlava on 03,October,2019
 * Updated by Shaun McDonald on 2021/06/26
 * Last modified on 26/06/2021, 05:50
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

abstract class BaseFragment(
    protected val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : Fragment(), IProgressView, DIAware {

    private lateinit var sharedViewModel: SharedViewModel
    override val di by closestDI()
    private val shareFactory: SharedViewModelFactory by instance()
    private val armoury: XIArmoury by instance()
    protected var coordinator: View? = null

    // =========================================================================================
    internal open var currentLocation: LocationModel? = null
    private lateinit var locationViewModel: LocationViewModel
    private val locationFactory by instance<LocationViewModelFactory>()
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false

    // =========================================================================================
    lateinit var ACTIVITY: MainActivity

    companion object {
        const val GPS_REQUEST = 100
        const val LOCATION_REQUEST = 101

        @JvmField
        var bounce: Animation? = null

        @JvmField
        var bounce_short: Animation? = null

        @JvmField
        var bounce_long: Animation? = null

        @JvmField
        var bounce_soft: Animation? = null

        @JvmField
        var bounce_250: Animation? = null

        @JvmField
        var bounce_500: Animation? = null

        @JvmField
        var bounce_750: Animation? = null

        @JvmField
        var bounce_1000: Animation? = null

        @JvmField
        var scale: Animation? = null

        @JvmField
        var scale_light: Animation? = null

        @JvmField
        var click: Animation? = null

        @JvmField
        var shake_delay: Animation? = null

        @JvmField
        var shake: Animation? = null

        @JvmField
        var shake_long: Animation? = null

        @JvmField
        var shake_longer: Animation? = null

        var animations: Animations? = null
    }

    override fun onResume() {
        super.onResume()
        armoury.writeFutureTimestamp()
        ViewLogger.logView(this)
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to [Activity.onPause] of the containing
     * Activity's lifecycle.
     */
    override fun onPause() {
        super.onPause()
        IndefiniteSnackbar.hide()
    }

    override fun onStart() {
        super.onStart()
        invokeLocationAction()
    }

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
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                LOCATION_REQUEST
            )
        }
    }

    private fun startLocationUpdate() {
        locationViewModel.getLocationData().observe(
            this,
            { it ->
                currentLocation = it
            }
        )
    }

    private fun isPermissionsGranted() =
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun shouldShowRequestPermissionRationale() =
        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) && ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationViewModel = this.run {
            ViewModelProvider(this, locationFactory).get(LocationViewModel::class.java)
        }

        GPSUtils(requireContext()).activateGPS(object : GPSUtils.OnGpsListener {

            override fun gpsStatus(isGPSEnable: Boolean) {
                gpsEnabled = isGPSEnable
            }
        })
        animations = Animations(requireContext().applicationContext)
    }

    private fun initAnimations() {
        click = AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.click)
        bounce = AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce)
        bounce_short =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_short)
        bounce_long =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_long)
        bounce_250 =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_250)
        bounce_500 =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_500)
        bounce_750 =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_750)
        bounce_1000 =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_1000)
        bounce_soft =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.bounce_soft)
        shake_delay =
            AnimationUtils.loadAnimation(
                requireContext().applicationContext,
                R.anim.shake_long_delay
            )
        shake = AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.shake)
        shake_long =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.shake_long)
        shake_longer =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.shake_longer)
        scale = AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.scale)
        scale_light =
            AnimationUtils.loadAnimation(requireContext().applicationContext, R.anim.scale_light)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bounce = AnimationUtils.loadAnimation(context.applicationContext, R.anim.bounce)
        shake = AnimationUtils.loadAnimation(context.applicationContext, R.anim.shake)
        ACTIVITY = context as MainActivity
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        coordinator = view.findViewById(R.id.coordinator)
        sharedViewModel = ViewModelProvider(this.requireActivity(), shareFactory).get(SharedViewModel::class.java)
        initAnimations()
    }

    /**
     * Hide keyboard.
     */
    fun Activity.hideKeyboard() {
        val view = this.currentFocus

        if (view != null) {
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // Helper function that checks connectivity
    // before allowing the suspend function to execute
    protected suspend fun connectedCheck(
        call: suspend () -> Unit,
    ) {
        when {
            !isOnline() -> noConnectionWarning()
            !hasInternet() -> noInternetWarning()
            !isServiceAvailable() -> noServicesWarning()
            else -> {
                call.invoke()
            }
        }
    }

    override fun toast(resid: Int) {
        if (!activity?.isFinishing!!) toast(getString(resid))
    }

    override fun toast(resid: String?) {
        resid?.let {
            if (!activity?.isFinishing!!) {
                sharedViewModel.setMessage(resid)
            }
        }
    }

    private fun isOnline(): Boolean {
        return ServiceUtil.isNetworkAvailable(this.requireContext().applicationContext)
    }

    protected fun noConnectionWarning() {
        extensionToast(
            message = "Please ensure that you have a valid data or wifi connection",
            style = ToastStyle.NO_INTERNET,
            position = BOTTOM,
            duration = LONG
        )
    }

    private fun hasInternet(): Boolean {
        return ServiceUtil.isHostAvailable("dns.google.com", DNS_PORT, FIVE_SECONDS)
    }

    private fun isServiceAvailable(): Boolean {
        return ServiceUtil.isHostAvailable(BuildConfig.API_HOST, SSL_PORT, FIVE_SECONDS)
    }

    private fun noServicesWarning() {
        extensionToast(
            message = "RRM services are unreachable, try again later ...",
            style = ToastStyle.NO_INTERNET,
            position = BOTTOM,
            duration = LONG
        )
    }

    protected fun noInternetWarning() {
        extensionToast(
            message = "No internet access, try again later ...",
            style = ToastStyle.NO_INTERNET,
            position = BOTTOM,
            duration = LONG
        )
    }

    fun toggleLongRunning(toggle: Boolean) {
        sharedViewModel.takingPhotos = toggle
        sharedViewModel.toggleLongRunning(toggle)
    }

    private fun snackError(coordinator: View?, string: String?) {
        if (coordinator != null) {
            val snackBar = Snackbar.make(coordinator, string!!, 3000)
            snackBar.view.setBackgroundColor(Color.RED)
            snackBar.view.startAnimation(shake)
            snackBar.show()
        } else Timber.e("x -> coordinator is null")
    }

    fun snackError(string: String?) {
        snackError(coordinator, string)
    }

    fun takingPhotos() {
        sharedViewModel.takingPhotos = true
    }

    fun photosDone() {
        sharedViewModel.takingPhotos = false
    }

    abstract fun onCreateOptionsMenu(menu: Menu): Boolean
}
