/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
package za.co.xisystems.itis_rrm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import com.jakewharton.threetenabp.AndroidThreeTen
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bind
import org.kodein.di.eagerSingleton
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.singleton
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
import za.co.xisystems.itis_rrm.extensions.exitApplication
import za.co.xisystems.itis_rrm.forge.XIArmoury
import za.co.xisystems.itis_rrm.logging.LameCrashLibrary
import za.co.xisystems.itis_rrm.services.DeferredLocationRepository
import za.co.xisystems.itis_rrm.services.DeferredLocationViewModelFactory
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.auth.model.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.LocationViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvejobs.ApproveJobsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.approvemeasure.ApproveMeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.corrections.CorrectionsViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.create.CreateViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.estmeasure.MeasureViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.home.HomeViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.unsubmitted.UnSubmittedViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.WorkViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.work.goto_work_location.GoToViewModelFactory
import za.co.xisystems.itis_rrm.utils.PhotoUtil

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

open class MainApp : Application(), DIAware {

    private var activityReferences = 0
    private var isActivityChangingConfigurations = false
    override val di = DI.lazy {

        import(androidXModule(this@MainApp))
        bind { eagerSingleton { XIArmoury.getInstance(instance()) } }
        bind { singleton { AppDatabase(instance(), instance()) } }
        bind { eagerSingleton { PhotoUtil.getInstance(instance()) } }
        bind { singleton { NetworkConnectionInterceptor(instance()) } }
        bind { singleton { BaseConnectionApi(instance()) } }
        bind { singleton { PreferenceProvider(instance()) } }
        bind { singleton { UserRepository(instance(), instance()) } }
        bind { singleton { OfflineDataRepository(api = instance(), appDb = instance(), photoUtil = instance()) } }
        bind { singleton { JobCreationDataRepository(instance(), instance(), instance()) } }
        bind { singleton { JobApprovalDataRepository(instance(), instance()) } }
        bind { singleton { WorkDataRepository(instance(), instance(), instance()) } }
        bind { singleton { MeasureCreationDataRepository(instance(), instance(), instance()) } }
        bind { singleton { MeasureApprovalDataRepository(instance(), instance()) } }
        bind { singleton { DeferredLocationRepository(instance(), instance()) } }
        bind { provider { GoToViewModelFactory(instance()) } }
        bind { provider { AuthViewModelFactory(instance(), instance(), instance(), this@MainApp) } }

        bind {
            provider {
                HomeViewModelFactory(
                    repository = instance(),
                    offlineDataRepository = instance(),
                    application = this@MainApp
                )
            }
        }

        bind {
            provider {
                CreateViewModelFactory(
                    jobCreationDataRepository = instance(),
                    userRepository = instance(),
                    application = this@MainApp,
                    photoUtil = instance()
                )
            }
        }

        bind {
            provider {
                ApproveMeasureViewModelFactory(
                    this@MainApp,
                    instance(),
                    instance(),
                    instance(),
                )
            }
        }

        bind {
            provider {
                ApproveJobsViewModelFactory(
                    this@MainApp,
                    instance(),
                    instance()
                )
            }
        }

        bind { provider { DeferredLocationViewModelFactory(instance(), instance(), this@MainApp) } }
        bind { provider { MeasureViewModelFactory(this@MainApp, instance(), instance()) } }
        bind { provider { UnSubmittedViewModelFactory(instance()) } }
        bind { provider { WorkViewModelFactory(instance(), instance(), this@MainApp) } }
        bind { provider { CorrectionsViewModelFactory(instance()) } }
        bind { provider { SettingsViewModelFactory(instance()) } }
        bind { provider { MainActivityViewModelFactory(instance()) } }
        bind { provider { LocationViewModelFactory(this@MainApp) } }
        bind { provider { SharedViewModelFactory(instance()) } }
    }

    override fun onCreate() {
        super.onCreate()
        // Time-zone support for better times with Room
        AndroidThreeTen.init(this)

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            @SuppressLint("SourceLockedOrientationActivity")
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                p0.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            override fun onActivityStarted(p0: Activity) {
                if (++activityReferences == 1 && !isActivityChangingConfigurations) {
                    // Application has entered foreground
                    Timber.d("App in foreground.")
                }
            }

            override fun onActivityResumed(p0: Activity) {
                // Not interested in this
            }

            override fun onActivityPaused(p0: Activity) {
                // Not interested in this
            }

            override fun onActivityStopped(p0: Activity) {
                // Count activity references
                isActivityChangingConfigurations = p0.isChangingConfigurations

                if (--activityReferences == 0 && !isActivityChangingConfigurations) {
                    Timber.d("App in background.")
                    when (p0 is BaseActivity && XIArmoury.checkTimeout() && !p0.takingPhotos) {
                        true -> {
                            if (p0 is LoginActivity) {
                                p0.exitApplication()
                            } else {
                                p0.logoutApplication()
                            }
                        }
                        else -> Timber.i("Long running external process")
                    }
                }
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
                when (priority) {
                    Log.ERROR -> {
                        LameCrashLibrary.logError(t)
                    }
                    Log.WARN -> {
                        LameCrashLibrary.logWarning(t)
                    }
                }
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        AppDatabase.closeDown()
        XIArmoury.closeArmoury()
    }
}
