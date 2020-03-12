package za.co.xisystems.itis_rrm.ui.mainview._fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.Animations
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.HorizontalProgressBar
import za.co.xisystems.itis_rrm.utils.ViewLogger

/**
 * Created by Francis Mahlava on 03,October,2019
 */


abstract class BaseFragment : Fragment(), IProgressView, HorizontalProgressBar{
    protected var progressView: IProgressView? = null
    @JvmField var bounce: Animation? = null
    @JvmField var bounce_short: Animation? = null
    @JvmField var bounce_long: Animation? = null
    @JvmField var bounce_soft: Animation? = null
    @JvmField var bounce_250: Animation? = null
    @JvmField var bounce_500: Animation? = null
    @JvmField var bounce_750: Animation? = null
    @JvmField var bounce_1000: Animation? = null
    @JvmField var scale: Animation? = null
    @JvmField var scale_light: Animation? = null
    @JvmField var click: Animation? = null
    @JvmField var shake_delay: Animation? = null
    @JvmField var shake: Animation? = null
    @JvmField var shake_long: Animation? = null
    @JvmField var shake_longer: Animation? = null
    protected var coordinator: View? = null

    var anims: Animations? = null


    override fun onResume() {
        super.onResume()
        ViewLogger.logView(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anims = Animations(context!!.applicationContext)
        initAnims()
    }

    fun initAnims() {
        click = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.click)
        bounce = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce)
        bounce_short = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_short)
        bounce_long = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_long)
        bounce_250 = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_250)
        bounce_500 = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_500)
        bounce_750 = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_750)
        bounce_1000 = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_1000)
        bounce_soft = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.bounce_soft)
        shake_delay = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.shake_long_delay)
        shake = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.shake)
        shake_long = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.shake_long)
        shake_longer = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.shake_longer)
        scale = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.scale)
        scale_light = AnimationUtils.loadAnimation(context!!.applicationContext, R.anim.scale_light)
    }

    override fun onDetach() {
        super.onDetach()
        progressView = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bounce = AnimationUtils.loadAnimation(context.applicationContext, R.anim.bounce)
        shake = AnimationUtils.loadAnimation(context.applicationContext, R.anim.shake)
        if (context is IProgressView) {
            progressView = context
        }
    }
//    override fun onDestroyView() {
//        super.onDestroyView()
//        if (view != null) {
//            val parent = view!!.parent as ViewGroup
//            parent?.removeAllViews()
//        }
//    }
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        coordinator = view.findViewById(R.id.coordinator)
    }

//    fun toast(string: String?) {
//        if (activity != null) Toast.makeText(activity, string, Toast.LENGTH_LONG).show()
//    }
//
//    fun toast(string: Int) {
//        if (activity != null) Toast.makeText(activity, string, Toast.LENGTH_LONG).show()
//    }

    fun setDataProgressDialog(context: Context, message: String):ProgressDialog{
        //Assuming that you are using fragments.//
        val prog = ProgressDialog(context)
        prog.setTitle(getString(R.string.please_wait))
        prog.setMessage(message)
        prog.setCancelable(false)
        prog.isIndeterminate = true
        prog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//        prog.show()
        return prog
    }

    fun disMissProgressDialog(prog : ProgressDialog){
        prog.dismiss()
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
        if (!activity?.isFinishing!!) Toast.makeText(context?.applicationContext, resid, Toast.LENGTH_LONG).show()
    }

    fun snackError(coordinator: View?, string: String?) {
        if (coordinator != null) {
            val snackbar = Snackbar.make(coordinator, string!!, 3000)
            snackbar.view.setBackgroundColor(Color.RED)
            snackbar.view.startAnimation(shake)
            snackbar.show()
        } else Log.e("x-", "coordinator is null")
    }

    fun snackError(string: String?) {
        snackError(coordinator, string)
    }

    private var progressDialog: ProgressDialog? = null

    private fun setProgressStyleHorizontal() {
        this.progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    }

    fun removeProgressPercentages() {
        progressDialog?.setProgressNumberFormat(null)
        progressDialog?.setProgressPercentFormat(null)
    }

    fun initializeProgressPercentage() {
        this.progressDialog?.max = 100
        this.progressDialog?.progress = 0
    }

    override fun showHorizontalProgressDialog(message: CharSequence?) {
        if (null == this.progressDialog) {
            this.progressDialog = ProgressDialog(context?.applicationContext, android.R.style.Theme_DeviceDefault_Dialog)
            this.progressDialog?.isIndeterminate = true
            this.progressDialog?.setCancelable(false)
            setProgressStyleHorizontal()
            removeProgressPercentages()
            if (this.progressDialog?.isIndeterminate == false) initializeProgressPercentage()
        }
        this.progressDialog?.setMessage(message)
        this.progressDialog?.show()
    }

    override fun setProgressBarMessage(message: CharSequence?) {
        this.progressDialog?.setMessage(message)
    }

    fun stepProgressDialogTo(num: Int) {
        this.progressDialog?.progress = num
    }

    override fun dismissProgressDialog() {
        if (this.progressDialog != null && !activity?.isFinishing!!) {
            this.progressDialog?.dismiss()
            this.progressDialog = null
        }
    }

    override fun showProgressDialog(vararg messages: String?) {
        if (null != messages) {
            var message = messages[0]
            if (messages.size > 1) {
                for (i in 1 until messages.size) {
                    message += "\n" + messages[i]
                }
            }
            showHorizontalProgressDialog(message)
        }
    }


    abstract fun onCreateOptionsMenu(menu: Menu): Boolean
}