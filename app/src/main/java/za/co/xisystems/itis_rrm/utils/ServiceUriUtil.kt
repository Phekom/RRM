package za.co.xisystems.itis_rrm.utils

import android.util.Log
import za.co.xisystems.itis_rrm.BuildConfig

/**
 * Created by Mauritz Mollentze on 2015/06/22.
 * Updated by Pieter Jacobs during 2016/02, 2016/03, 2016/06, 2016/07.
 * Updated by Francis Mahlava during 2019/11.
 */
// TODO improve this class. a singleton may not be needed. we may only need a static string uri
class ServiceUriUtil {
    var webServiceUri: String? = null

    companion object {
        private val TAG = ServiceUriUtil::class.java.simpleName
        private var instance: ServiceUriUtil? = null
        fun initInstance() {
            instance = ServiceUriUtil()
            instance!!.webServiceUri = webServiceRootUri
        }

        fun getInstance(): ServiceUriUtil? {
            if (null == instance) {
                Log.e(
                    TAG,
                    "Singleton was destroyed or Never initialized"
                )
                initInstance()
                instance!!.webServiceUri = webServiceRootUri
            }
            return instance
        }

        val webServiceRootUri: String
            get() = BuildConfig.API_HOST

        val serverUriFriendlyString: String
            get() {
                var url = BuildConfig.API_HOST
                url = url.replace("http://", "")
                val x = url.indexOf("/")
                return if (x > -1) url.substring(0, x) else url
            }
    }
}
