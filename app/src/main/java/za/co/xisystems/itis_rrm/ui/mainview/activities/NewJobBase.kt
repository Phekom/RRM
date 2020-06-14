package za.co.xisystems.itis_rrm.ui.mainview.activities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.functions.Consumer
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.Animations
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView
import za.co.xisystems.itis_rrm.data.network.OfflineListener
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.HorizontalProgressBar
import za.co.xisystems.itis_rrm.utils.RxUtils
import za.co.xisystems.itis_rrm.utils.ViewLogger

/**
 * Created by Francis Mahlava on 2019/12/18.
 */
abstract class NewJobBase : AppCompatActivity(), OfflineListener, IProgressView,
    HorizontalProgressBar {

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

    private var anims: Animations? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        anims = Animations(this)
        initAnims()
    }

    private fun initAnims() {
        click = AnimationUtils.loadAnimation(this, R.anim.click)
        bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        bounce_short = AnimationUtils.loadAnimation(this, R.anim.bounce_short)
        bounce_long = AnimationUtils.loadAnimation(this, R.anim.bounce_long)
        bounce_250 = AnimationUtils.loadAnimation(this, R.anim.bounce_250)
        bounce_500 = AnimationUtils.loadAnimation(this, R.anim.bounce_500)
        bounce_750 = AnimationUtils.loadAnimation(this, R.anim.bounce_750)
        bounce_1000 = AnimationUtils.loadAnimation(this, R.anim.bounce_1000)
        bounce_soft = AnimationUtils.loadAnimation(this, R.anim.bounce_soft)
        shake_delay = AnimationUtils.loadAnimation(this, R.anim.shake_long_delay)
        shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        shake_long = AnimationUtils.loadAnimation(this, R.anim.shake_long)
        shake_longer = AnimationUtils.loadAnimation(this, R.anim.shake_longer)
        scale = AnimationUtils.loadAnimation(this, R.anim.scale)
        scale_light = AnimationUtils.loadAnimation(this, R.anim.scale_light)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun toast(resid: Int) {
        if (!isFinishing) toast(getString(resid))
    }

    override fun toast(resid: String?) {
        if (!isFinishing) Toast.makeText(this, resid, Toast.LENGTH_LONG).show()
    }

    fun toastShort(resid: Int) {
        if (!isFinishing) toastShort(getString(resid))
    }

    private fun toastShort(resid: String) {
        if (!isFinishing) Toast.makeText(this, resid, Toast.LENGTH_SHORT).show()
    }

    protected fun enableBackNavigation() {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
        }
    }

    fun <T> scheduleOnUi(o: Observable<T>): Observable<T> {
        return RxUtils.schedule(o)
    }

    fun getThrowableConsumer(): Consumer<Throwable> {
        return Consumer<Throwable> { t ->
            toastShort(t?.message ?: "Some error occur!")
            dismissProgressDialog()
        }
    }

    fun finishOk() {
        setResult(RESULT_OK)
        finish()
    }

    fun finishOk(message: String?) {
        toast(message)
        this.finish(null, RESULT_OK)
    }

    fun finishOk(data: Intent?) {
        this.finish(data, RESULT_OK)
    }

    fun finish(message: String, resultCode: Int) {
        toast(message)
        this.finish(null, resultCode)
    }

    private fun finish(data: Intent?, resultCode: Int) {
        if (data == null) this.setResult(resultCode)
        else this.setResult(resultCode, data)
        this.finish()
    }

    override fun onResume() {
        super.onResume()
        ViewLogger.logView(this)
    }

    // TODO clean progress dialog code
    private var progressDialog: ProgressDialog? = null

    private fun setProgressStyleHorizontal() {
        this.progressDialog?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
    }

    private fun removeProgressPercentages() {
        progressDialog?.setProgressNumberFormat(null)
        progressDialog?.setProgressPercentFormat(null)
    }

    private fun initializeProgressPercentage() {
        this.progressDialog?.max = 100
        this.progressDialog?.progress = 0
    }

    override fun showHorizontalProgressDialog(message: CharSequence?) {
        if (null == this.progressDialog) {
            this.progressDialog = ProgressDialog(this, R.style.ThemeOverlay_MaterialComponents_Dialog)
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
        if (this.progressDialog != null && !this.isFinishing) {
            this.progressDialog?.dismiss()
            this.progressDialog = null
        }
    }

    override fun showProgressDialog(vararg messages: String?) {
        if (messages != null) {
            var message = messages[0]
            for (i in 1 until messages.size) {
                message += "\n" + messages[i]
            }
            showHorizontalProgressDialog(message)
        }
    }
}
