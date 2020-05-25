/**
 * Created by Francis Mahlava on 2019/10/23.
 */
package za.co.xisystems.itis_rrm

import leakcanary.AppWatcher
import leakcanary.LeakCanary


/**
 * Created by Francis Mahlava on 2019/10/23.
 */
open class DebugMainApp : MainApp() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            AppWatcher.config = AppWatcher.config.copy(watchFragmentViews = false)
            LeakCanary.config = LeakCanary.config.copy(retainedVisibleThreshold = 3)
        }
    }
}
