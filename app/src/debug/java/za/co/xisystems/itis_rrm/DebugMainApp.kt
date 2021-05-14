/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
package za.co.xisystems.itis_rrm

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.gu.toolargetool.TooLargeTool
import leakcanary.AppWatcher
import leakcanary.LeakCanary
import org.kodein.di.KodeinAware
import timber.log.Timber

/**
 * Created by Francis Mahlava on 2019/10/23.
 *
 */
open class DebugMainApp : MainApp(), KodeinAware {

    override fun onCreate() {

        // Shaun McDonald - 2020/04/02 - Added LeakCanary and AppWatcher to debug build.
        if (BuildConfig.DEBUG) {
            Timber.plant(HyperlinkDebugTree())
            LeakCanary.config = LeakCanary.config.copy(retainedVisibleThreshold = 5)
            AppWatcher.config = AppWatcher.config.copy(watchFragmentViews = true)
            // Shaun McDonald - 2020/05/01 - Have coroutines log out state changes
            System.setProperty("kotlinx.coroutines.debug", "on")
            // Shaun McDonald - 2021/01/30 - Added TooLargeTool to track bundle sizes
            TooLargeTool.startLogging(this)
            // Shaun McDonald - 2021/02/09 - Added StrictMode to help with optimization
            SetStrictMode()
        }
        super.onCreate()
    }

    // Strict mode halps us catch leaks and bad practices super quick
    private fun SetStrictMode() {

        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork() // or .detectAll() for all detectable problems
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }

    /** A tree which adds the filename, line-number and method call when debugging. */
    private class HyperlinkDebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            with(element) {
                return ("($fileName:$lineNumber)$methodName")
            }
        }
    }
}
