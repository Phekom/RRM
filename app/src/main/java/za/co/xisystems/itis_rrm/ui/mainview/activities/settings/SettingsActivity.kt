package za.co.xisystems.itis_rrm.ui.mainview.activities.settings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Job
import org.kodein.di.DIAware
import org.kodein.di.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToastStyle
import za.co.xisystems.itis_rrm.MainApp
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.notifications.ToastDuration
import za.co.xisystems.itis_rrm.custom.notifications.ToastGravity
import za.co.xisystems.itis_rrm.custom.notifications.ToastStyle
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.databinding.SettingsActivityBinding
import za.co.xisystems.itis_rrm.delegates.viewBinding
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.auth.RegisterActivity
import za.co.xisystems.itis_rrm.ui.auth.ResetPinActivity
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.ui.extensions.extensionToast
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import java.util.concurrent.CancellationException

class SettingsActivity : BaseActivity() {
    private var canResert: Boolean = false
    private var canResertApp: Boolean = false
    override val di by lazy { (applicationContext as MainApp).di }
    private var userDTO: UserDTO? = null
    private lateinit var settingsViewModel: SettingsViewModel
    private val factory: SettingsViewModelFactory by instance()
    private val binding by viewBinding(SettingsActivityBinding::inflate)
    private lateinit var synchJob: Job

    companion object {
        private val TAG = SettingsActivity::class.java.simpleName
        const val HOME = "general_switch"

        var switch: SwitchMaterial? = null
        const val PREFS_NAME = "DarkModeSwitch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        settingsViewModel = ViewModelProvider(this, factory).get(SettingsViewModel::class.java)

        Coroutines.main {
            val loggedInUser = settingsViewModel.user.await()
            loggedInUser.observe(this) { user ->
                // Register the user
                if (user != null) {
                    userDTO = user
                    binding.username1.text = user.userName
                }
            }
        }


//        binding.resetRoleButton.setOnClickListener {
//            synchAlertdialog(userDTO)
//        }

        binding.buttonResetApp.setOnClickListener {
            Coroutines.main {
                val jobList = settingsViewModel.checkUnsubmittedList(ActivityIdConstants.JOB_ESTIMATE )
                val measureList = settingsViewModel.checkUnsubmittedMeasureList(ActivityIdConstants.JOB_ESTIMATE)
                canResert = jobList.isEmpty()
                canResertApp = measureList.isEmpty()

                if (canResert){

                    if (canResertApp){
                        val builder = AlertDialog.Builder(
                            this@SettingsActivity, android.R.style.Theme_DeviceDefault_Dialog
                        )
                        builder.setTitle(R.string.confirm)
                        builder.setMessage(R.string.all_data_will_be_deleted_are_you_sure)
                        // Yes button
                        builder.setPositiveButton(R.string.yes) { _, _ ->
                            Coroutines.main {
                                settingsViewModel.deleteAllData()
                                // Take user back to the Registration screen
                                Intent(this, RegisterActivity::class.java).also { home ->
                                    home.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(home)
                                }
                            }
                        }
                        // No button
                        builder.setNegativeButton(R.string.no) { dialog, _ ->
                            // Do nothing but close dialog
                            dialog.dismiss()
                        }
                        val alert = builder.create()
                        alert.show()
                    }else{
                        promptUserToSubmitMeasuresFirst()
                    }
                } else {
                    promptUserToSubmitJobsFirst()
                }
            }

        }

        binding.resetPinButton.setOnClickListener {
            val resetPinIntent = Intent(applicationContext, ResetPinActivity::class.java)
            startActivity(resetPinIntent)
            finish()
        }
    }

    override fun startLongRunningTask() {
        Timber.e("starting task...")
        // Make UI untouchable for duration of task
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    override fun endLongRunningTask() {
        Timber.d("stopping task..." )
        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

//    private fun synchAlertdialog(userDTO: UserDTO?) {
//        val textEntryView = layoutInflater.inflate(R.layout.user_alert_dialog, null)
//        val userpass = textEntryView.findViewById<View>(R.id.registeredUserPass) as EditText
//        val username = textEntryView.findViewById<View>(R.id.registeredUser) as TextView
//
//        val alert: androidx.appcompat.app.AlertDialog = androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
//            .setView(textEntryView)
//            .setIcon(R.drawable.ic_baseline_lock_24px)
//            .setTitle(R.string.user_access)
//            .setMessage("Enter Password to Refresh Your Access")
//            .setNegativeButton("Cancel", null)
//            .setPositiveButton("Submit", null).create()
//
//        alert.setOnShowListener { dialog ->
//            binding.apply {
//                Coroutines.main {
//                    username.text = userDTO!!.userName
//                    val enter = alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
//                    val cancel = alert.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE)
//                    cancel.setOnClickListener { dialog.dismiss() }
//                    enter.setOnClickListener {
//                        userSync(username.text.toString(),userpass.text.toString())
//                        dialog.dismiss()
//                    }
//                }
//            }
//        }
//
//        alert.show()
//    }



    private val bigSyncObserver = Observer<XIResult<Boolean>?> {
        Coroutines.main {
            handleBigSync(it)
        }
    }

    private fun handleBigSync(signal: XIResult<Boolean>?) {
        Timber.d("$signal")
        signal?.let { result ->
            when (result) {
                is XIResult.Success -> {
                    extensionToast(
                        message = "Sync Complete",
                        style = ToastStyle.SUCCESS,
                        position = ToastGravity.BOTTOM,
                        duration = ToastDuration.LONG
                    )
                }
                is XIResult.Status -> {
                    extensionToast(
                        message = result.message,
                        style = ToastStyle.INFO,
                        position = ToastGravity.BOTTOM
                    )
                }
                is XIResult.Error -> {
                    synchJob.cancel(CancellationException(result.message))

                    extensionToast(
                        title = "Sync Failed",
                        message = result.message,
                        style = ToastStyle.ERROR
                    )
                }
                is XIResult.Progress -> {
                    // showProgress(result.isLoading)
                }
                is XIResult.ProgressUpdate -> {
                    // handleProgressUpdate(result)
                }
                is XIResult.RestException -> {
                    Timber.e("$result")
                }
            }
        }
    }

    private fun promptUserToSubmitJobsFirst() = Coroutines.ui {
        val syncDialog: AlertDialog.Builder =
            AlertDialog.Builder(this)
                .setTitle("Resetting App Not Possible")
                .setMessage(getString(R.string.you_have_out_standing_jobs))
                .setCancelable(false)
                .setIcon(R.drawable.ic_warning_yellow)
                .setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent(baseContext, MainActivity::class.java))
                }

        syncDialog.show()
    }

    private fun promptUserToSubmitMeasuresFirst() = Coroutines.ui {
        val syncDialog: AlertDialog.Builder =
            AlertDialog.Builder(this)
                .setTitle("Resetting App Not Possible")
                .setMessage(getString(R.string.you_have_out_standing_measures))
                .setCancelable(false)
                .setIcon(R.drawable.ic_warning_yellow)
                .setPositiveButton(R.string.ok) { _, _ ->
                    startActivity(Intent(baseContext, MainActivity::class.java))
                }

        syncDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(baseContext, MainActivity::class.java))
    }
}
