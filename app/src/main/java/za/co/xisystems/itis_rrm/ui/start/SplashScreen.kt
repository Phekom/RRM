package za.co.xisystems.itis_rrm.ui.start

import am.appwise.components.ni.NoInternetDialog
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.kodein.di.DIAware
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.MainApp
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.custom.events.XIEvent
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.network.PermissionController
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.data.preferences.MyAppPrefsManager
import za.co.xisystems.itis_rrm.databinding.ActivitySplashScreenBinding
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.auth.RegisterActivity
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ConnectionLiveData
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil.isNetworkConnected

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity(), DIAware {
    override val di by lazy { (applicationContext as MainApp).di }

    private lateinit var splashViewModel: SplashActivityViewModel
    private val factory: SplashActivityViewModelFactory by instance()
    private var uiScope = UiLifecycleScope()
    lateinit var binding: ActivitySplashScreenBinding
    var myAppPrefsManager: MyAppPrefsManager? = null
    var healthState: MutableLiveData<XIEvent<XIResult<String>>> = MutableLiveData()
    lateinit var noInternetDialog: NoInternetDialog  //? = null
    private lateinit var connectionLiveData: ConnectionLiveData
    var isInternetConnected: Boolean = false
    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.INTERNET
    )


    override fun onStart() {
        super.onStart()
        googlePlayServicesCheck(this)
    }

    companion object {
        val TAG: String = SplashScreen::class.java.simpleName
        private const val PERMISSION_REQUEST = 42
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        splashViewModel = this.run {
            ViewModelProvider(this, factory)[SplashActivityViewModel::class.java]
        }
        startPermissionRequest(permissions)
        googlePlayServicesCheck(this)
        networkEnabled = isNetworkConnected(this)
        noInternetDialog = NoInternetDialog.Builder(this@SplashScreen).build()

        binding.progressBar.visibility = View.VISIBLE
        myAppPrefsManager = MyAppPrefsManager(this)
        // getInternetConnectionResult(noInternetDialog)
        connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this) { isNetworkAvailable ->
            isNetworkAvailable?.let {
                if (it) {
                    updateUI(it)
                } else {
                    updateUI(false)
                }
            }
        }
    }

    private fun updateUI(connected: Boolean) {
        when {
            connected -> {
                Coroutines.main {
                    val healthResponse = splashViewModel.healthCheck()
                    if (healthResponse) {
                        val newVersion = BuildConfig.VERSION_NAME
                        //val isUserWorking = splashViewModel.checkUser(myAppPrefsManager)
                        val user = splashViewModel.user.await()
                        user.observe(this@SplashScreen, Observer { userInstance ->
                            if (userInstance != null) {
                                Coroutines.ui {
                                    val isUserWorking = splashViewModel.checkUser(userInstance.userName , myAppPrefsManager)
                                    if (isUserWorking == "N") {
                                        Snackbar.make(
                                            this@SplashScreen,
                                            binding.root,
                                            "You Seem To have No Access Please Contact Support",
                                            Snackbar.LENGTH_INDEFINITE
                                        ).setAction(getString(R.string.ok), View.OnClickListener
                                        {
                                            // continueWithout()
                                        }).show()
                                    }else{
                                        val response = splashViewModel.getAppVersionCheck(newVersion)
                                        if (response.errorMessage.equals("")) {
                                            acquireUser(noInternetDialog)
                                        } else {
                                            promptUserToForNewVersion(response)
                                        }
                                    }
                                }
                            }else{
                                startActivity(Intent(this@SplashScreen, RegisterActivity::class.java))
                            }
                        })

                    } else {
                        Snackbar.make(
                            this@SplashScreen,
                            binding.root,
                            "You Seem To have Little or No Internet Connection Click OK to Continue",
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(getString(R.string.ok), View.OnClickListener
                        {
                            continueWithout()
                        }).show()
                    }
                }
            }
            else -> {
                if (networkEnabled) {
                    updateUI(networkEnabled)
                } else {
                    // noInternetDialog.showDialog()
                    if (noInternetDialog.isShowing) {
                        continueWithout()
                    }
                }
            }
        }
    }



    private fun continueWithout() {
        Snackbar.make(
            this@SplashScreen,
            binding.root, "Note!! You Are Working Offline",
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.continue_forward), View.OnClickListener
        // Handle the Retry Button Click
        {
            startActivity(Intent(baseContext, LoginActivity::class.java))
        }).show()
    }

    private fun acquireUser(noInternetDialog: NoInternetDialog) {
        uiScope.launch(uiScope.coroutineContext) {
            val user = splashViewModel.user.await()
            user.observe(this@SplashScreen, Observer { userInstance ->
                if (userInstance != null) {
                    if (noInternetDialog.isShowing) {
                        ToastUtils().toastShort(this@SplashScreen, "Check Internet Connection")
                    } else {
                        Coroutines.main {
                            try {
                                    // Navigate to MainActivity
                                    startActivity(
                                        Intent(
                                            this@SplashScreen,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()

                            } catch (t: Throwable) {
                                ToastUtils().toastShort(
                                    this@SplashScreen,
                                    "You Seem To have Little or No Network Connection"
                                )
                            } finally {
                                Snackbar.make(
                                    this@SplashScreen,
                                    binding.root, "Continue Without Data Connection!!",
                                    Snackbar.LENGTH_INDEFINITE
                                ).setAction(getString(R.string.ok), View.OnClickListener
                                // Handle the Retry Button Click
                                {
                                    startActivity(
                                        Intent(
                                            baseContext,
                                            LoginActivity::class.java
                                        )
                                    )
                                }).show()
                            }

                        }
                    }

                } else {
                    startActivity(Intent(this@SplashScreen, RegisterActivity::class.java))
                }
            })
        }

    }

    private fun promptUserToForNewVersion(response: VersionCheckResponse) = Coroutines.ui {

        if (BuildConfig.API_HOST.contains("https://itisqa")) {
            val qaVersionlink = "http://itisqa.nra.co.za/portal/mobile/rrm_app.apk"
            val syncDialog: AlertDialog.Builder =
                AlertDialog.Builder(this)
                    .setTitle("New QA Version Available")
                    .setMessage(response.errorMessage + getString(R.string.new_line) + getString(R.string.new_line) + "Release Date " + response.releaseDate)
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_warning)
                    .setNegativeButton(R.string.continue_old) { _, _ ->
                        startActivity(Intent(baseContext, LoginActivity::class.java))
                    }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        val intent = Intent(
                            Intent.ACTION_VIEW, Uri.parse(qaVersionlink)
                        )
                        startActivity(intent)
                    }
            syncDialog.show()
        } else {

            val prodVersionlink = "https://itis.nra.co.za/portal/mobile/rrm_app.apk"
            val syncDialog: AlertDialog.Builder =
                AlertDialog.Builder(this)
                    .setTitle("New Version Available")
                    .setMessage(response.errorMessage + getString(R.string.new_line) + getString(R.string.new_line) + "Release Date " + response.releaseDate)
                    .setCancelable(false)
                    .setIcon(R.drawable.ic_warning)
                    .setNegativeButton(R.string.continue_old) { _, _ ->
                        startActivity(Intent(baseContext, LoginActivity::class.java))
                    }
                    .setPositiveButton(R.string.ok) { _, _ ->
                        val intent = Intent(
                            Intent.ACTION_VIEW, Uri.parse(prodVersionlink)
                        )
                        startActivity(intent)
                    }

            syncDialog.show()
        }

    }




    private fun startPermissionRequest(permissions: Array<String>): Boolean {
        var allAccess = true
        for (i in permissions.indices)
            if (checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                allAccess = false
            } else {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
            }
        return allAccess
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    private fun googlePlayServicesCheck(activity: Activity) {
        val resultCode = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(activity.applicationContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            // This dialog will help the user update to the latest GooglePlayServices
            val dialog =
                GoogleApiAvailability.getInstance().getErrorDialog(activity, resultCode, 0)
            dialog?.show()
        }
        if (PermissionController.checkPermissionsEnabled(applicationContext)) {
            // googleApiClient!!.connect()
        } else {
            PermissionController.startPermissionRequests(activity)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }


}

