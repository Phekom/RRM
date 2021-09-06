package za.co.xisystems.itis_rrm.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines

abstract class BaseActivity : AppCompatActivity(), DIAware {
    override val di by closestDI()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private val armoury: XIArmoury by instance()

    val takingPhotos get() = sharedViewModel.takingPhotos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Plug in the SharedViewModel
        this.sharedViewModel = this.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        }

        sharedViewModel.longRunning.observe(this, {
            armoury.writeFutureTimestamp()
            when (it) {
                true -> this.startLongRunningTask()
                false -> this.endLongRunningTask()
            }
        })

        if (savedInstanceState == null) {
            armoury.writeFutureTimestamp()
        }
    }

    abstract fun startLongRunningTask()

    abstract fun endLongRunningTask()

    override fun onResume() {
        super.onResume()
        checkTimeout()
    }

    private fun checkTimeout() {
        if (armoury.checkTimeout() &&
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
        if (armoury.checkTimeout()) {
            // val pid = Process.myPid()
            // Process.killProcess(pid)
            // val intent = Intent(Intent.ACTION_MAIN)
            // intent.addCategory(Intent.CATEGORY_HOME)
            // startActivity(intent)
        }
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
}
