package za.co.xisystems.itis_rrm.ui.base

import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines

abstract class BaseActivity : AppCompatActivity(), KodeinAware {
    override val kodein by kodein()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private val armoury: XIArmoury by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Plug in the SharedViewModel
        this.sharedViewModel = this.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        }

        // Set MotionToast to use Sanral colours
        MotionToast.setErrorColor(R.color.sanral_dark_red)
        MotionToast.setSuccessColor(R.color.sanral_dark_green)
        MotionToast.setWarningColor(R.color.warning_color)
        MotionToast.setInfoColor(R.color.dark_slate_gray)
        MotionToast.setDeleteColor(R.color.dark_slate_gray)

        sharedViewModel.longRunning.observe(this, {
            armoury.writeFutureTimestamp()
            when (it) {
                true -> this.startLongRunningTask()
                false -> this.endLongRunningTask()
            }
        })

        sharedViewModel.message.observe(this, {
            toastMessage(it.toString())
        })

        sharedViewModel.colorMessage.observe(this, {
            it?.let {
                toastMessage(it)
            }
        })

        sharedViewModel.actionCaption.observe(this, {
            setCaption(it)
        })

        if (savedInstanceState == null) {
            armoury.writeFutureTimestamp()
        }
    }

    abstract fun startLongRunningTask()

    abstract fun endLongRunningTask()

    private fun toastMessage(message: String) {
        ToastUtils().toastShort(applicationContext, message)
    }

    private fun setCaption(caption: String) {
        sharedViewModel.originalCaption = supportActionBar?.title.toString()
        supportActionBar?.title = caption
    }

    override fun onResume() {
        super.onResume()
        checkTimeout()
    }

    private fun checkTimeout() {
        val timeInMillis = System.currentTimeMillis()
        val timeDiff = timeInMillis - armoury.getTimestamp()
        Timber.d("TimeDiff: $timeDiff")
        if (timeDiff >= Constants.TEN_MINUTES &&
            !sharedViewModel.takingPhotos
        ) {
            logoutApplication()
        }
    }

    fun logoutApplication() {
        Coroutines.main {
            sharedViewModel.logOut()
            withContext(Dispatchers.Main.immediate) {
                Intent(this@BaseActivity, LoginActivity::class.java).also { login ->
                    login.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(login)
                }
                finish()
            }
        }
    }

    fun exitApplication() {
        val pid = Process.myPid()
        Process.killProcess(pid)
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        armoury.writeFutureTimestamp()
    }

    override fun onUserInteraction() {
        Timber.d("User interaction!!")
        super.onUserInteraction()
        armoury.writeFutureTimestamp()
    }

    private fun toastMessage(
        colorToast: ColorToast
    ) {
        MotionToast.createColorToast(
            context = this,
            title = colorToast.title,
            message = colorToast.message,
            style = colorToast.style.getValue(),
            position = colorToast.gravity.getValue(),
            duration = colorToast.duration.getValue(),
            font = ResourcesCompat.getFont(this, R.font.helvetica_regular)
        )
    }
}
