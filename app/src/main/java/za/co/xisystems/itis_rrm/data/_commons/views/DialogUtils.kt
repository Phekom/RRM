package za.co.xisystems.itis_rrm.data._commons.views

import android.R.style
import android.app.Activity
import androidx.appcompat.app.AlertDialog

class DialogUtils {
    private var isLightTheme = false
    fun setThemeToLight(): DialogUtils {
        isLightTheme = true
        return this
    }

    fun setThemeToDark(): DialogUtils {
        isLightTheme = false
        return this
    }

    private fun createBuilder(activity: Activity): AlertDialog.Builder {
        return if (isLightTheme) AlertDialog.Builder(activity) else AlertDialog.Builder(
            activity,
            style.Theme_DeviceDefault_Dialog
        )
    }

    @JvmOverloads
    fun showAreYouSure(
        activity: Activity,
        message: String,
        positiveCallBack: PositiveCallBack?,
        negativeCallBack: NegativeCallBack? = null
    ) {
        createBuilder(activity)
            .setMessage("$message: Are you sure?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog?.dismiss()
                positiveCallBack?.onPositive()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog?.dismiss()
                negativeCallBack?.onNegative()
            }.show()
    }

    interface PositiveCallBack {
        fun onPositive()
    }

    interface NegativeCallBack {
        fun onNegative()
    }

    init {
        setThemeToDark() // default theme
    }
}