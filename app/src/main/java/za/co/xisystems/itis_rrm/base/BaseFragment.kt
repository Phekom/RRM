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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.Animations
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView
import za.co.xisystems.itis_rrm.utils.ViewLogger

/**
 * Created by Francis Mahlava on 03,October,2019
 */
// R.layout.fragment_home
//
abstract class BaseFragment(layoutContentId: Int) : Fragment(), IProgressView {

    companion object {

        @JvmField
        var layoutContentId: Int? = null

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
            if (!activity?.isFinishing!!) sharpToast(resid)
        }
    }

    protected fun sharpToast(resId: Int, motionType: String = MotionToast.TOAST_INFO, position: Int = MotionToast.GRAVITY_BOTTOM, duration: Int = MotionToast.LONG_DURATION) {
        val message = getString(resId)
        sharpToast(message, motionType, position, duration)
    }

    protected fun sharpToast(message: String, motionType: String = MotionToast.TOAST_INFO, position: Int = MotionToast.GRAVITY_BOTTOM, duration: Int = MotionToast.LONG_DURATION) {
        if (!activity?.isFinishing!!)
            MotionToast.createColorToast(
                context = requireActivity(),
                message = message,
                style = motionType,
                position = position,
                duration = duration,
                font = ResourcesCompat.getFont(requireActivity(), R.font.helvetica_regular)
            )
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

    abstract fun onCreateOptionsMenu(menu: Menu): Boolean
}
