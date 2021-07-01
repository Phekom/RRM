package za.co.xisystems.itis_rrm.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants.DNS_PORT
import za.co.xisystems.itis_rrm.constants.Constants.FIVE_SECONDS
import za.co.xisystems.itis_rrm.constants.Constants.SSL_PORT
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.isRecoverableException
import za.co.xisystems.itis_rrm.custom.views.IndefiniteSnackbar
import za.co.xisystems.itis_rrm.data._commons.Animations
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.ViewLogger
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.SHORT
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.NO_INTERNET
import kotlin.coroutines.CoroutineContext

/**
 * Created by Francis Mahlava on 03,October,2019
 * Updated by Shaun McDonald on 2021/06/26
 * Last modified on 26/06/2021, 05:50
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

abstract class BaseFragment : Fragment(), IProgressView, KodeinAware {

    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    protected var coordinator: View? = null

    companion object {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        coroutineContext: CoroutineContext
    ) {
        when {
            !isOnline() -> noConnectionWarning()
            !hasInternet() -> noInternetWarning()
            !isServiceAvailable() -> noServicesWarning()
            else -> {
                withContext(coroutineContext) {
                    call.invoke()
                }
            }
        }
    }

    override fun toast(resid: Int) {
        if (!activity?.isFinishing!!) toast(getString(resid))
    }

    override fun toast(resid: String?) {
        resid?.let {
            if (!activity?.isFinishing!!) {
                sharpToast(resource = resid)
            }
        }
    }

    protected fun sharpToast(resource: String) {
        sharpToast(message = resource)
    }

    protected fun sharpToast(
        title: String? = null,
        resId: Int,
        style: ToastStyle = INFO,
        position: ToastGravity = BOTTOM,
        duration: ToastDuration = SHORT
    ) {
        if (!activity?.isFinishing!!) {
            val message = getString(resId)
            sharpToast(title, message, style, position, duration)
        }
    }

    fun sharpToast(
        title: String? = null,
        message: String,
        style: ToastStyle = INFO,
        position: ToastGravity = BOTTOM,
        duration: ToastDuration = SHORT
    ) {
        if (!activity?.isFinishing!!) {
            sharedViewModel.setColorMessage(
                title = title,
                message = message,
                style = style,
                position = position,
                duration = duration

            )
        }
    }

    private fun isOnline(): Boolean {
        return ServiceUtil.isNetworkAvailable(this.requireContext().applicationContext)
    }

    protected fun noConnectionWarning() {
        sharpToast(
            message = "Please ensure that you have a valid data or wifi connection",
            style = NO_INTERNET,
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
        sharpToast(
            message = "RRM services are unreachable, try again later ...",
            style = NO_INTERNET,
            position = BOTTOM,
            duration = LONG
        )
    }

    protected fun noInternetWarning() {
        sharpToast(
            message = "No internet access, try again later ...",
            style = NO_INTERNET,
            position = BOTTOM,
            duration = LONG
        )
    }

    protected fun toggleLongRunning(toggle: Boolean) {
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

    /**
     * if the exception is connectivity-related, give the user the option to retry.
     * Shaun McDonald - 2020/06/01
     */
    protected fun crashGuard(view: View, throwable: XIError, refreshAction: (() -> Unit)? = null) {

        when (throwable.isRecoverableException()) {

            true -> {
                if (refreshAction != null) {
                    XIErrorHandler.handleError(
                        view = view,
                        throwable = throwable,
                        shouldShowSnackBar = true,
                        refreshAction = { refreshAction() }
                    )
                }
            }
            else -> {

                sharpToast(
                    message = throwable.message,
                    style = ERROR,
                    position = BOTTOM,
                    duration = LONG
                )

                XIErrorHandler.handleError(
                    view = view,
                    throwable = throwable,
                    shouldToast = false,
                    shouldShowSnackBar = false
                )
            }
        }
    }

    fun takingPhotos() {
        sharedViewModel.takingPhotos = true
    }

    fun photosDone() {
        sharedViewModel.takingPhotos = false
    }

    abstract fun onCreateOptionsMenu(menu: Menu): Boolean
}
