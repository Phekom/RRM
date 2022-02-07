package za.co.xisystems.itis_rrm.utils.image_capture.helper

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.core.app.ActivityCompat

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
object PermissionHelper {

    fun checkPermission(activity: Activity, permission: String, listener: PermissionAskListener) {
        if (!hasSelfPermission(activity, permission)) {
            if (shouldShowRequestPermissionRationale(activity, permission)) {
                listener.onPermissionPreviouslyDenied()
            } else {
                if (PreferenceHelper.isFirstTimeAskingPermission(activity, permission)) {
                    PreferenceHelper.firstTimeAskingPermission(activity, permission, false)
                    listener.onNeedPermission()
                } else {
                    listener.onPermissionDisabled()
                }
            }
        } else {
            listener.onPermissionGranted()
        }
    }

    fun openAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activity.startActivity(intent)
    }

    fun asArray(vararg permissions: String): Array<String?> {
        require(permissions.isNotEmpty()) { "There is no given permission" }
        val dest = arrayOfNulls<String>(permissions.size)
        for (i in permissions.indices) {
            dest[i] = permissions[i]
        }
        return dest
    }

    fun hasGranted(grantResult: Int): Boolean {
        return grantResult == PackageManager.PERMISSION_GRANTED
    }

    fun hasGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (!hasGranted(result)) {
                return false
            }
        }
        return true
    }

    private fun hasSelfPermission(context: Context, permission: String): Boolean {
        return if (shouldAskPermission()) {
            permissionHasGranted(context, permission)
        } else true
    }

    fun hasSelfPermissions(context: Context, permissions: Array<String>): Boolean {
        if (shouldAskPermission()) {
            for (permission in permissions) {
                if (!permissionHasGranted(context, permission)) {
                    return false
                }
            }
        }
        return true
    }


    fun isPermissionInManifest(context: Context, permission: String): Boolean {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val permissions = packageInfo.requestedPermissions

        if (permissions.isNullOrEmpty())
            return false

        for (perm in permissions) {
            if (perm == permission)
                return true
        }

        return false
    }


    fun requestAllPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        if (shouldAskPermission()) {
            internalRequestPermissions(activity, permissions, requestCode)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun internalRequestPermissions(
        activity: Activity?,
        permissions: Array<String>,
        requestCode: Int
    ) {
        requireNotNull(activity) { "Given activity is null." }
        activity.requestPermissions(permissions, requestCode)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun permissionHasGranted(context: Context, permission: String): Boolean {
        return hasGranted(context.checkSelfPermission(permission))
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    private fun shouldAskPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    fun shouldShowRequestPermissionRationale(activity: Activity?, permission: String): Boolean {
        if (activity != null) {
            return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
        return false
    }

    interface PermissionAskListener {
        fun onNeedPermission()
        fun onPermissionPreviouslyDenied()
        fun onPermissionDisabled()
        fun onPermissionGranted()
    }
}