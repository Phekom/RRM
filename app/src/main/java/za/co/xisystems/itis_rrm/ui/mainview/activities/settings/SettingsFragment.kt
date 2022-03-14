package za.co.xisystems.itis_rrm.ui.mainview.activities.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import za.co.xisystems.itis_rrm.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val isChecked = false

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
