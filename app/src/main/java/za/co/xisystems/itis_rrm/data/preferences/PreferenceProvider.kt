package za.co.xisystems.itis_rrm.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

private const val KEY_SAVED_AT = "key_saved_at"
/**
 * Created by Francis Mahlava on 2019/10/18.
 */
open class PreferenceProvider(
    context: Context
) {

    val appContext: Context = context.applicationContext

    protected val preferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)

    fun savelastSavedAt(savedAt: String) {
        preferences.edit().putString(
            KEY_SAVED_AT,
            savedAt
        ).apply()
    }

    fun getLastSavedAt(): String? {
        return preferences.getString(KEY_SAVED_AT, null)
    }
}
