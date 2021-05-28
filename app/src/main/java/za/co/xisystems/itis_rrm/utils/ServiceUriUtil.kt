/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.utils

import timber.log.Timber
import za.co.xisystems.itis_rrm.BuildConfig

/**
 * Created by Mauritz Mollentze on 2015/06/22.
 * Updated by Pieter Jacobs during 2016/02, 2016/03, 2016/06, 2016/07.
 * Updated by Francis Mahlava during 2019/11.
 */

class ServiceUriUtil {
    var webServiceUri: String? = null
    var webServiceHost: String? = null

    companion object {
        private val TAG = ServiceUriUtil::class.java.simpleName
        private var instance: ServiceUriUtil? = null
        private fun initInstance() {
            instance = ServiceUriUtil()
            instance!!.webServiceUri = webServiceRootUri
            instance!!.webServiceHost = serverUriFriendlyString
        }

        fun getInstance(): ServiceUriUtil? {
            if (null == instance) {
                Timber.d("Initializing ServiceUriUtil")
                initInstance()
            }
            return instance
        }

        private val webServiceRootUri: String
            get() = BuildConfig.API_HOST

        private val serverUriFriendlyString: String
            get() {
                var url = BuildConfig.API_HOST
                url = url.replace("https://", "")
                val x = url.indexOf("/")
                return if (x > -1) url.substring(0, x) else url
            }
    }
}
