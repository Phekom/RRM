package za.co.xisystems.itis_rrm.ui.mainview.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.switchmaterial.SwitchMaterial
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.databinding.SettingsActivityBinding
import za.co.xisystems.itis_rrm.delegates.viewBinding
import za.co.xisystems.itis_rrm.ui.auth.RegisterActivity
import za.co.xisystems.itis_rrm.ui.auth.ResetPinActivity
import za.co.xisystems.itis_rrm.utils.Coroutines

class SettingsActivity: AppCompatActivity(), DIAware {

    override val di by closestDI()
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
        }

        binding.resetPinButton.setOnClickListener {
            val resetPinIntent = Intent(applicationContext, ResetPinActivity::class.java)
            startActivity(resetPinIntent)
            finish()
        }
    }
}
