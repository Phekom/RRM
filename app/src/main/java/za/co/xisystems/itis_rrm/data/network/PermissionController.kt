package za.co.xisystems.itis_rrm.data.network

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

/**
 * Created by Pieter Jacobs on 2016/08/25.
 * Updated by Pieter Jacobs during 2016/08.
 */
object PermissionController {
    val permissions: List<String>
        get() {
            val permissions: MutableList<String> =
                ArrayList()
            permissions.add(Manifest.permission.READ_PHONE_STATE)
            permissions.add(Manifest.permission.READ_SMS)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.CAMERA)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return permissions
        }

    @JvmOverloads
    fun startPermissionRequests(
        activity: Activity?
    ) {
        val permissions =
            permissions
        for (i in permissions.indices) {
            val permission = permissions[i]
            val requestCode = i + 1
            val checkResult = ContextCompat.checkSelfPermission(activity!!, permission)
            if (checkResult != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                requestCode
            )
        }
    }

    fun checkPermissionsEnabled(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions =
                permissions
            for (permission in permissions) {
                val checkResult = ContextCompat.checkSelfPermission(context!!, permission)
                if (checkResult != PackageManager.PERMISSION_GRANTED) return false
            }
        }
        return true
    }
}