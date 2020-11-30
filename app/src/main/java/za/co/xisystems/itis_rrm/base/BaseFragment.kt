package za.co.xisystems.itis_rrm.base

// import android.app.ProgressDialog
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
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIError
import za.co.xisystems.itis_rrm.custom.results.isConnectivityException
import za.co.xisystems.itis_rrm.data._commons.Animations
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.utils.ViewLogger
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.LONG
import za.co.xisystems.itis_rrm.utils.enums.ToastDuration.SHORT
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.BOTTOM
import za.co.xisystems.itis_rrm.utils.enums.ToastGravity.CENTER
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.ERROR
import za.co.xisystems.itis_rrm.utils.enums.ToastStyle.INFO

/**
 * Created by Francis Mahlava on 03,October,2019
 */
// R.layout.fragment_home
//
abstract class BaseFragment(layoutContentId: Int) : Fragment(), IProgressView, KodeinAware {

    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()

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
        protected var coordinator: View? = null

        var animations: Animations? = null
    }

    override fun onResume() {
        super.onResume()
        ViewLogger.logView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animations = Animations(requireContext().applicationContext)
        initAnimations()
    }

    /**
     * Called when the fragment's activity has been created and this
     * fragment's view hierarchy instantiated.  It can be used to do final
     * initialization once these pieces are in place, such as retrieving
     * views or restoring state.  It is also useful for fragments that use
     * [.setRetainInstance] to retain their instance,
     * as this callback tells the fragment when it is fully associated with
     * the new activity instance.  This is called after [.onCreateView]
     * and before [.onViewStateRestored].
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sharedViewModel = activity?.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
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

    override fun toast(resid: Int) {
        if (!activity?.isFinishing!!) toast(getString(resid))
    }

    override fun toast(resid: String?) {
        resid?.let {
            if (!activity?.isFinishing!!)
                sharpToast(resource = resid)
        }
    }

    protected fun sharpToast(resource: String){
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

    protected fun sharpToast(
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
                duration = duration,
            )
        }
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
    protected fun crashGuard(view: View, throwable: XIError, refreshAction: () -> Unit) {

        when (throwable.isConnectivityException()) {

            true -> {
                XIErrorHandler.handleError(
                    view = view,
                    throwable = throwable,
                    shouldShowSnackBar = true,
                    refreshAction = refreshAction
                )
            }
            else -> {
                sharpToast(
                    message = throwable.message,
                    style = ERROR,
                    position = CENTER,
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

    abstract fun onCreateOptionsMenu(menu: Menu): Boolean
}
