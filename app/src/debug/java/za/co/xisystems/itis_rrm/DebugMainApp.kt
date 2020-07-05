/**
 * Created by Francis Mahlava on 2019/10/23.
 */
package za.co.xisystems.itis_rrm

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
        super.onCreate()

        // Shaun McDonald - 2020/04/02 - Added LeakCanary and AppWatcher to debug build.
        if (BuildConfig.DEBUG) {
            Timber.plant(HyperlinkDebugTree())
            LeakCanary.config = LeakCanary.config.copy(retainedVisibleThreshold = 3)
            AppWatcher.config = AppWatcher.config.copy(watchFragmentViews = true)
        }
    }

    /** A tree which adds the filename, line-number and method call when debugging. */
    private class HyperlinkDebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String? {
            with(element) {
                return ("($fileName:$lineNumber)$methodName")
            }
        }
    }
}
