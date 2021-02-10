package za.co.xisystems.itis_rrm.ui.mainview.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.Coroutines

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        Coroutines.io {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val isChecked = false
            withContext(Dispatchers.Main.immediate) {
                val myTheme =
                    findPreference<SwitchPreferenceCompat>(SettingsActivity.HOME)
                myTheme!!.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { _, newValue ->
                        if (newValue == !isChecked) {

                            (activity as SettingsActivity?)?.delegate?.localNightMode =
                                AppCompatDelegate.MODE_NIGHT_YES
                        } else {

                            (activity as SettingsActivity?)!!.delegate.localNightMode =
                                AppCompatDelegate.MODE_NIGHT_NO
                        }
                        true
                    }
            }
        }
    }
}
