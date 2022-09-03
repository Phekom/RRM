package za.co.xisystems.itis_rrm.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey.Builder
import androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM

/**
 * Created by Francis Mahlava on 2021/10/18.
 * MyAppPrefsManager handles some Prefs of ITIS Application
 */
class MyAppPrefsManager(context: Context) {

    private val masterKey = Builder(context.applicationContext).setKeyScheme(AES256_GCM).build()

    private var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private var prefsEditor: SharedPreferences.Editor = sharedPreferences.edit()

    fun setUserLanguageId(langID: Int) {
        prefsEditor.putInt(
            USER_LANGUAGE_ID,
            langID
        )
        prefsEditor.commit()
    }

    fun getUserLanguageId(): Int? {
        return sharedPreferences.getInt(
            USER_LANGUAGE_ID,
            1
        )
    }

    fun setDueDatePeriod(langCode: String?) {
        prefsEditor.putString(
            USER_LANGUAGE_CODE,
            langCode
        )
        prefsEditor.commit()
    }

    fun getDueDatePeriod(): String? {
        return sharedPreferences.getString(
            USER_LANGUAGE_CODE,
            ""
        )
    }

    fun getApplicationVersion(): String? {
        return sharedPreferences.getString(
            APPLICATION_VERSION,
            ""
        )
    }

    fun setApplicationVersion(applicationVersion: String?) {
        prefsEditor.putString(
            APPLICATION_VERSION,
            applicationVersion
        )
        prefsEditor.commit()
    }

    fun setUserLoggedIn(isUserLoggedIn: Boolean) {
        prefsEditor.putBoolean(
            IS_USER_LOGGED_IN,
            isUserLoggedIn
        )
        prefsEditor.commit()
    }

    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(
            IS_USER_LOGGED_IN,
            false
        )
    }

    fun setFirstTimeLaunch(isFirstTimeLaunch: Boolean) {
        prefsEditor.putBoolean(
            IS_FIRST_TIME_LAUNCH,
            isFirstTimeLaunch
        )
        prefsEditor.commit()
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferences.getBoolean(
            IS_FIRST_TIME_LAUNCH,
            true
        )
    }

    fun setPushNotificationsEnabled(isPushNotificationsEnabled: Boolean) {
        prefsEditor.putBoolean(
            IS_PUSH_NOTIFICATIONS_ENABLED,
            isPushNotificationsEnabled
        )
        prefsEditor.commit()
    }

    fun isPushNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(
            IS_PUSH_NOTIFICATIONS_ENABLED,
            true
        )
    }

    fun setLocalNotificationsEnabled(isLocalNotificationsEnabled: Boolean) {
        prefsEditor.putBoolean(
            IS_LOCAL_NOTIFICATIONS_ENABLED,
            isLocalNotificationsEnabled
        )
        prefsEditor.commit()
    }

    fun isLocalNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(
            IS_LOCAL_NOTIFICATIONS_ENABLED,
            true
        )
    }

    fun setLocalNotificationsTitle(localNotificationsTitle: String?) {
        prefsEditor.putString(
            LOCAL_NOTIFICATIONS_TITLE,
            localNotificationsTitle
        )
        prefsEditor.commit()
    }

    fun getLocalNotificationsTitle(): String? {
        return sharedPreferences.getString(
            LOCAL_NOTIFICATIONS_TITLE,
            "CdmKart"
        )
    }

    fun setLocalNotificationsDuration(localNotificationsDuration: String?) {
        prefsEditor.putString(
            LOCAL_NOTIFICATIONS_DURATION,
            localNotificationsDuration
        )
        prefsEditor.commit()
    }

    fun getLocalNotificationsDuration(): String? {
        return sharedPreferences.getString(
            LOCAL_NOTIFICATIONS_DURATION,
            "day"
        )
    }

    fun setLocalNotificationsDescription(localNotificationsDescription: String?) {
        prefsEditor.putString(
            LOCAL_NOTIFICATIONS_DESCRIPTION,
            localNotificationsDescription
        )
        prefsEditor.commit()
    }

    fun getLocalNotificationsDescription(): String? {
        return sharedPreferences.getString(
            LOCAL_NOTIFICATIONS_DESCRIPTION,
            "Check bundle of New Structures"
        )
    }

    fun setSkip_For_Again(isChecked: Boolean) {
        prefsEditor.putBoolean(
            Skip_For_Again,
            isChecked
        )
        prefsEditor.commit()
    }

    fun getSkip_For_Again(): Boolean {
        return sharedPreferences.getBoolean(
            Skip_For_Again,
            false
        )
    }

    fun getFliterBy(): String? {
        return sharedPreferences.getString(
            FILTER_BY,
            ""
        )
    }

    fun setFliterBy(phonenumber: String?) {
        prefsEditor.putString(
            FILTER_BY,
            phonenumber
        )
        prefsEditor.commit()
    }

    fun getActionRequired(): String? {
        return sharedPreferences.getString(
            ACTION_REQUIRED,
            ""
        )
    }

    fun setActionRequired(country_code: String?) {
        prefsEditor.putString(
            ACTION_REQUIRED,
            country_code
        )
        prefsEditor.commit()
    }

    fun getSigned(): String? {
        return sharedPreferences.getString(
            SIGNATURE,
            ""
        )
    }

    fun setSigned(signature: String?) {
        prefsEditor.putString(
            SIGNATURE,
            signature
        )
        prefsEditor.commit()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(
            AUTHTOKEN,
            ""
        )
    }

    fun setAccessToken(signature: String?) {
        prefsEditor.putString(
            AUTHTOKEN,
            signature
        )
        prefsEditor.commit()
    }

    companion object {
        private const val PREF_NAME = "RRM_App_Prefs"
        private const val USER_LANGUAGE_ID = "language_ID"
        private const val USER_LANGUAGE_CODE = "language_Code"
        private const val APPLICATION_VERSION = "application_version"
        private const val IS_USER_LOGGED_IN = "isLogged_in"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val IS_PUSH_NOTIFICATIONS_ENABLED = "isPushNotificationsEnabled"
        private const val IS_LOCAL_NOTIFICATIONS_ENABLED = "isLocalNotificationsEnabled"
        private const val LOCAL_NOTIFICATIONS_TITLE = "localNotificationsTitle"
        private const val LOCAL_NOTIFICATIONS_DURATION = "localNotificationsDuration"
        private const val LOCAL_NOTIFICATIONS_DESCRIPTION = "localNotificationsDescription"
        private const val Skip_For_Again = "skipMessage"
        private const val CUSTOMER_PHONE = "customer_phone"
        private const val ACTION_REQUIRED = "country_code"
        private const val PRIVATE_MODE = 0
        private const val SIGNATURE = "signature"
        private const val FILTER_BY = "filter_by"
        private const val AUTHTOKEN = "accesstoken"
    }

// 	init {
// 		sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
// 		prefsEditor = sharedPreferences.edit()
// 	}
}
