package za.co.xisystems.itis_rrm.ui.mainview.activities.settings

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import org.kodein.di.DIAware
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.MainApp
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.databinding.SettingsActivityBinding
import za.co.xisystems.itis_rrm.delegates.viewBinding
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.auth.RegisterActivity
import za.co.xisystems.itis_rrm.ui.auth.ResetPinActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines

class SettingsActivity : AppCompatActivity(), DIAware {
    private var canResert: Boolean = false
    override val di by lazy { (applicationContext as MainApp).di }

    private lateinit var settingsViewModel: SettingsViewModel
    private val factory: SettingsViewModelFactory by instance()
    private val binding by viewBinding(SettingsActivityBinding::inflate)

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
            loggedInUser.observe(this, { user ->
                // Register the user
                if (user != null) {
                    binding.username1.text = user.userName
                }
            })
        }

        binding.buttonResetApp.setOnClickListener {
            Coroutines.main {
                val jobList = settingsViewModel.checkUnsubmittedList(ActivityIdConstants.JOB_ESTIMATE )
                val measureList = settingsViewModel.checkUnsubmittedMeasureList(ActivityIdConstants.JOB_ESTIMATE)
                canResert = jobList.isEmpty() && measureList.isEmpty()

                if (canResert){
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(baseContext, MainActivity::class.java))
    }
}
