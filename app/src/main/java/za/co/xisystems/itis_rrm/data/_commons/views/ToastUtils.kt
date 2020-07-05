package za.co.xisystems.itis_rrm.data._commons.views

import android.content.Context
import android.widget.Toast
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.utils.ServiceUriUtil

class ToastUtils {
    fun toastShort(context: Context?, text: String?) {
        if (context != null) Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun toastLong(context: Context?, text: String?) {
        if (context != null) Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    fun toastVersion(context: Context?) {
        val versionName_flavor =
            "Version " + BuildConfig.VERSION_NAME + " - " + BuildConfig.FLAVOR + "\n"
        val build = "Build " + BuildConfig.VERSION_BUILD
        val date: String = BuildConfig.VERSION_BUILD_DATE
        toastLong(context, "$versionName_flavor$build @ $date")
    }

    fun toastServerAddress(context: Context?) {
        toastShort(context, ServiceUriUtil.getInstance()?.webServiceUri)
    }
}
