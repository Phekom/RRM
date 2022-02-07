package za.co.xisystems.itis_rrm.utils.image_capture.helper

import android.content.Context

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
object PreferenceHelper {

    private const val PREFS_FILE_NAME = "ImagePicker"

    @JvmStatic
    fun firstTimeAskingPermission(context: Context, permission: String, isFirstTime: Boolean) {
        val preferences = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        preferences.edit()
            .putBoolean(permission, isFirstTime)
            .apply()
    }

    @JvmStatic
    fun isFirstTimeAskingPermission(context: Context, permission: String): Boolean {
        return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
            .getBoolean(permission, true)
    }
}