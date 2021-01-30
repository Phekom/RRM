/**
 * Created by Francis Mahlava on 2019/10/23.
 */
package za.co.xisystems.itis_rrm

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import timber.log.Timber
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.NetworkConnectionInterceptor
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.data.repositories.JobApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.MeasureApprovalDataRepository
import za.co.xisystems.itis_rrm.data.repositories.MeasureCreationDataRepository
import za.co.xisystems.itis_rrm.data.repositories.OfflineDataRepository
import za.co.xisystems.itis_rrm.data.repositories.UserRepository
import za.co.xisystems.itis_rrm.data.repositories.WorkDataRepository
import za.co.xisystems.itis_rrm.logging.LameCrashLibrary
import za.co.xisystems.itis_rrm.ui.auth.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.LocationViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.corrections.CorrectionsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.home.HomeViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
open class MainApp : Application(), KodeinAware {

    override val kodein = Kodein.lazy {

        import(androidXModule(this@MainApp))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { BaseConnectionApi(instance()) }

        bind() from singleton { AppDatabase(instance()) }
        bind() from singleton { PreferenceProvider(instance()) }

        bind() from singleton { UserRepository(instance(), instance()) }

        bind() from singleton { OfflineDataRepository(instance(), instance()) }

        bind() from singleton { JobCreationDataRepository(instance(), instance()) }
        bind() from singleton { JobApprovalDataRepository(instance(), instance()) }
        bind() from singleton { WorkDataRepository(instance(), instance()) }
        bind() from singleton { MeasureCreationDataRepository(instance(), instance()) }
        bind() from singleton { MeasureApprovalDataRepository(instance(), instance()) }

        bind() from provider { AuthViewModelFactory(instance(), instance()) }
        bind() from provider {
            HomeViewModelFactory(
                instance(),
                instance()
            )
        }
        bind() from provider { CreateViewModelFactory(instance()) }
        bind() from provider {
            ApproveMeasureViewModelFactory(
                this@MainApp,
                instance(),
                instance()
            )
        }
        bind() from provider {
            ApproveJobsViewModelFactory(
                this@MainApp,
                instance(),
                instance()
            )
        }

        bind() from provider { MeasureViewModelFactory(this@MainApp, instance(), instance()) }
        bind() from provider { UnSubmittedViewModelFactory(instance()) }
        bind() from provider { WorkViewModelFactory(instance(), instance()) }
        bind() from provider { CorrectionsViewModelFactory(instance()) }
        bind() from provider { SettingsViewModelFactory(instance()) }
        bind() from provider { MainActivityViewModelFactory(instance()) }
        bind() from provider { LocationViewModelFactory(this@MainApp) }

        bind() from provider { SharedViewModelFactory() }
        bind() from provider { SharedViewModel() }
    }

    override fun onCreate() {
        super.onCreate()



        // Time-zone support for better times with Room
        AndroidThreeTen.init(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                p0.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            override fun onActivityStarted(p0: Activity) {
                // Not interested in this
            }

            override fun onActivityResumed(p0: Activity) {
                // Not interested in this
            }

            override fun onActivityPaused(p0: Activity) {
                // Not interested in this
            }

            override fun onActivityStopped(p0: Activity) {
                // Not interested in this
            }

            override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
                // Not interested in this
            }

            override fun onActivityDestroyed(p0: Activity) {
                // Not interested in this
            }
        })
        Timber.plant(CrashReportingTree())
    }

    /** A tree which logs important information for crash reporting.  */
    private class CrashReportingTree : Timber.Tree() {
        override fun log(
            priority: Int,
            tag: String?,
            @NonNull message: String,
            t: Throwable?
        ) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return
            }
            LameCrashLibrary.log(priority, tag, message)
            if (t != null) {
                if (priority == Log.ERROR) {
                    LameCrashLibrary.logError(t)
                } else if (priority == Log.WARN) {
                    LameCrashLibrary.logWarning(t)
                }
            }
        }
    }
}
