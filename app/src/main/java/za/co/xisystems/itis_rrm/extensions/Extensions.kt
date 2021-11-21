package za.co.xisystems.itis_rrm.extensions

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.material.appbar.CollapsingToolbarLayout
import timber.log.Timber
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.ui.auth.Exiter
import za.co.xisystems.itis_rrm.utils.PhotoUtil
import za.co.xisystems.itis_rrm.utils.ServiceUtil

/**
 * Created by Shaun McDonald on 2020/06/05.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/

/***
 * A one shot observer that snags the data we need, and then shuts down.
 *
 * @receiver LiveData<T>
 * @param lifecycleOwner LifecycleOwner
 * @param observer Observer<T>
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(
        lifecycleOwner,
        object: Observer<T> {
            override fun onChanged(t: T?) {
                observer.onChanged(t)
                removeObserver(this)
            }
        }
    )
}

fun AppCompatActivity.applyToolbarMargin(toolbar: Toolbar) {
    toolbar.layoutParams = (
        toolbar.layoutParams
            as CollapsingToolbarLayout.LayoutParams
        ).apply {
            topMargin = getStatusBarSize()
        }
}

private fun AppCompatActivity.getStatusBarSize(): Int {
    val idStatusBarHeight =
        resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (idStatusBarHeight > 0) {
        resources.getDimensionPixelSize(idStatusBarHeight)
    } else {
        0
    }
}

fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

fun Activity.exitApplication() {
    this.run {
        PhotoUtil.shutdown()
        AppDatabase.closeDown()
        XIArmoury.closeArmoury()
        val relaunch = Intent(this, Exiter::class.java)
            .addFlags(
                FLAG_ACTIVITY_NEW_TASK // CLEAR_TASK requires this
                    or FLAG_ACTIVITY_CLEAR_TASK // finish everything else in the task
                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            ) // hide (remove, in this case) task from recent activities

        ContextCompat.startActivity(this, relaunch, null)
        finishAndRemoveTask()
    }
}

fun Context.uomForUI(uom: String): String {
    return when (uom.lowercase()) {
        "m2" -> {
            this.getString(R.string.uom_m2)
        }
        "m3" -> {
            this.getString(R.string.uom_m3)
        }
        "hour" -> {
            this.getString(R.string.uom_hour)
        }
        "l" -> {
            this.getString(R.string.uom_l)
        }
        "t" -> {
            this.getString(R.string.uom_t)
        }
        "no." -> {
            this.getString(R.string.uom_quantity)
        }
        "quantity" -> {
            this.getString(R.string.uom_quantity)
        }
        "prov sum" -> {
            this.getString(R.string.uom_prov_sum)
        }
        "lump sum" -> {
            this.getString(R.string.uom_lump_sum)
        }
        "none" -> {
            ""
        }
        else -> {
            "per ${uom.lowercase()}"
        }
    }
}

val Context.isConnected: Boolean get() = ServiceUtil.isNetworkConnected(this.applicationContext)

@Suppress("TooGenericExceptionCaught")
fun Fragment.checkLocationProviders() {
    val lm = this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    try {
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!gpsEnabled) { // notify user && !network_enabled
            displayPromptForEnablingGPS(this.requireActivity())
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

fun displayPromptForEnablingGPS(
    activity: Activity
) {

    val builder = AlertDialog.Builder(activity)
    builder.setCancelable(false)
    val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
    val message = ("Your GPS seems to be disabled, Please enable it to continue")
    builder.setMessage(message)
        .setPositiveButton("OK") { d, _ ->
            activity.startActivity(Intent(action))
            d.dismiss()
        }
    builder.create().show()
}
