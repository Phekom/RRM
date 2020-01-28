package za.co.xisystems.itis_rrm.ui.mainview.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.android.synthetic.main.settings_activity.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.ui.auth.RegisterActivity
import za.co.xisystems.itis_rrm.ui.auth.ResetPinActivity
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModel
import za.co.xisystems.itis_rrm.utils.Coroutines

class SettingsActivity : AppCompatActivity()  , KodeinAware {

    override val kodein by kodein()
    private lateinit var settingsViewModel: SettingsViewModel
    private val factory: SettingsViewModelFactory by instance()


    private var serviceVersionTextView: TextView? = null
    private val errorOccurredDuringRegistration = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        settingsViewModel = this?.run {
            ViewModelProviders.of(this, factory).get(SettingsViewModel::class.java)
        }


        Coroutines.main {
            val loggedInUser = settingsViewModel.user.await()
            loggedInUser.observe(this, Observer {user ->
                // Register the user
                if (user != null) {
                username1.text = user.userName
                }
            })

        }


        button_reset_app.setOnClickListener {
            val builder = AlertDialog.Builder(this@SettingsActivity, android.R.style.Theme_DeviceDefault_Dialog
            )
            builder.setTitle(R.string.confirm)
            builder.setMessage(R.string.all_data_will_be_deleted_are_you_sure)
            // Yes button
            builder.setPositiveButton(R.string.yes) { dialog, which ->
                // Clear out all the photos on the device
//                        deletePhotosInDirectory();
                Coroutines.main {
                settingsViewModel.deleteAllData()
                // Take user back to the Registration screen
                    Intent(this, RegisterActivity::class.java).also { home ->
                        home.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(home)
                    }
//                val registerIntent = Intent(applicationContext, RegisterActivity::class.java)
//                startActivity(registerIntent)
//                finish()
            }
            }
            // No button
            builder.setNegativeButton(R.string.no) { dialog, which ->
                // Do nothing but close dialog
                dialog.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }

        reset_pin_button.setOnClickListener {
            val resetPinIntent = Intent(applicationContext, ResetPinActivity::class.java)
            startActivity(resetPinIntent)
            finish()
        }










//        serviceVersionTextView = findViewById(R.id.serviceVersionTextView)
//
//
//        val serverTextView = findViewById<TextView>(R.id.serverUrlTextView)
        //        serverTextView.setText("\nServer: " + ServiceUriUtil.getWebServiceRootUri());
// Run device cleanup
//        cleanupDevice();
//        initRegisterInfo();
    }

    var isChecked = false

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onResume() { //            if (){
//            }
            super.onResume()
        }

        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?
        ) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val isChecked = false
            val mytheme =
                findPreference<SwitchPreferenceCompat>(HOME)
            mytheme!!.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    if (newValue == !isChecked) {
                        (activity as SettingsActivity?)!!.delegate.localNightMode =
                            AppCompatDelegate.MODE_NIGHT_YES
                    } else {
                        (activity as SettingsActivity?)!!.delegate.localNightMode =
                            AppCompatDelegate.MODE_NIGHT_NO
                    }
                    true
                }
        }
    }

    companion object {
        private val TAG = SettingsActivity::class.java.simpleName
        const val HOME = "general_switch"


        var switch: Switch? = null
        const val PREFS_NAME = "DarkeModeSwitch"
    }
}