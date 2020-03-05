
/**
 * Created by Francis Mahlava on 2019/10/23.
 */
package za.co.xisystems.itis_rrm

import android.app.Application
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.network.BaseConnectionApi
import za.co.xisystems.itis_rrm.data.network.NetworkConnectionInterceptor
import za.co.xisystems.itis_rrm.data.preferences.PreferenceProvider
import za.co.xisystems.itis_rrm.data.repositories.*
import za.co.xisystems.itis_rrm.ui.auth.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsViewModelFactory
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
        bind() from singleton { ContractsRepository() }

        bind() from singleton { HomeRepository(instance(), instance()) }
        bind() from singleton { OfflineDataRepository(instance(), instance(), instance()) }



        bind() from singleton { JobCreationDataRepository(instance(), instance(), instance()) }
        bind() from singleton { JobApprovalDataRepository(instance(), instance()) }
        bind() from singleton { WorkDataRepository(instance(), instance()) }
        bind() from singleton { MeasureCreationDataRepository(instance(), instance()) }
        bind() from singleton { MeasureApprovalDataRepository(instance(), instance()) }

//        bind<ID_Provider>() with singleton { IdProviderImpl(instance()) }

        bind() from provider { AuthViewModelFactory(instance(),instance()) }
        bind() from provider { HomeViewModelFactory(instance(),instance(),instance(),instance()) }
        bind() from provider { CreateViewModelFactory(instance() ) }
        bind() from provider { ApproveMeasureViewModelFactory(instance() ,instance()) }
        bind() from provider { ApproveJobsViewModelFactory(instance(),instance() ) }
        bind() from provider { MeasureViewModelFactory(instance(),instance() ) }
        bind() from provider { UnSubmittedViewModelFactory(instance() ) }
        bind() from provider{ WorkViewModelFactory(instance(),instance()) }
        bind() from provider{ CorrectionsViewModelFactory(instance()) }
        bind() from provider{ SettingsViewModelFactory(instance()) }
        bind() from provider{ MainActivityViewModelFactory(instance()) }

    }


}
















//class MainApp : Application() {
//
//    override fun onCreate() {
//        super.onCreate()
//        startKoin{
//            androidLogger()
//            androidContext(this@MainApp)
//            modules(appModule)
//            modules( viewmodelModule)
//        }
//
//    }
//
//
//}