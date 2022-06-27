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
import za.co.xisystems.itis_rrm.custom.errors.ReceptionException
import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
import za.co.xisystems.itis_rrm.custom.results.XIResult
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.data.network.PermissionController
import za.co.xisystems.itis_rrm.data.network.responses.VersionCheckResponse
import za.co.xisystems.itis_rrm.data.preferences.MyAppPrefsManager
import za.co.xisystems.itis_rrm.databinding.ActivitySplashScreenBinding
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.auth.RegisterActivity
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity(), DIAware {
    override val di by lazy { (applicationContext as MainApp).di }

    private lateinit var splashViewModel: SplashActivityViewModel
    private val factory: SplashActivityViewModelFactory by instance()
    private var uiScope = UiLifecycleScope()
    lateinit var binding: ActivitySplashScreenBinding
    var myAppPrefsManager: MyAppPrefsManager? = null

    private var gpsEnabled: Boolean = false
    private var networkEnabled: Boolean = false

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
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

        networkEnabled = ServiceUtil.isNetworkConnected(this)
        val noInternetDialog: NoInternetDialog = NoInternetDialog.Builder(this@SplashScreen).build()
        binding.progressBar.visibility = View.VISIBLE
        myAppPrefsManager = MyAppPrefsManager(this)
        getInternetConnectionResult(noInternetDialog)

    }




    private fun acquireUser(noInternetDialog: NoInternetDialog) {
            uiScope.launch(uiScope.coroutineContext) {
                val user = splashViewModel.user.await()
                user.observe(this@SplashScreen, Observer { userInstance ->
                    if (userInstance != null) {
                        if(noInternetDialog.isShowing){
                            ToastUtils().toastShort(this@SplashScreen, "Check Internet Connection")
                        }else{
                            Coroutines.main {
                                try {
                                    val newVersion = BuildConfig.VERSION_NAME
                                    val response = splashViewModel.getAppVersionCheck(newVersion)
                                    if (response.errorMessage.equals("")) {
                                        startActivity(Intent(baseContext, LoginActivity::class.java))
                                    } else {
                                        promptUserToForNewVersion(response)
                                    }
                                } catch (t: Throwable) {
                                   ToastUtils().toastShort(this@SplashScreen, "You Seem To have Little or No Network Connection")
                                }
//                                finally {
//                                    Snackbar.make(
//                                        this@SplashScreen,
//                                        binding.root, "Continue Without Data Connection!!",
//                                        Snackbar.LENGTH_INDEFINITE
//                                    ).setAction(getString(R.string.ok), View.OnClickListener
//                                    // Handle the Retry Button Click
//                                    {
//                                        startActivity(Intent(baseContext, LoginActivity::class.java))
//                                    }).show()
//                                }


                            }
                        }

                    } else {
                        startActivity(Intent(this@SplashScreen, RegisterActivity::class.java))
                    }
                })
            }

    }


    private fun getInternetConnectionResult(noInternetDialog: NoInternetDialog) {

        if (!startPermissionRequest(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
            Snackbar.make(
                this@SplashScreen,
                binding.root, "If You Have Allowed Permissions!!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.retry), View.OnClickListener
            // Handle the Retry Button Click
            {
                binding.progressBar.visibility = View.VISIBLE
                // Restart MyTask after 3 seconds
                getInternetConnectionResult(noInternetDialog)
            }).show()
        }else{
            if (!ServiceUtil.isNetworkConnected(this@SplashScreen)) {
                noInternetDialog.showDialog()
                Snackbar.make(
                    this@SplashScreen,
                    binding.root, "Error Service Host Un-Reachable Check Network And Data Connection!!",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.retry), View.OnClickListener
                // Handle the Retry Button Click
                {
                    binding.progressBar.visibility = View.VISIBLE
                    // Restart MyTask after 3 seconds
                    getInternetConnectionResult(noInternetDialog)
                }).show()
            } else {
                if (noInternetDialog.isShowing) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    acquireUser(noInternetDialog)
                }
            }
        }

    }


    private fun promptUserToForNewVersion(response: VersionCheckResponse) = Coroutines.ui {
        val prodVersionlink = "https://itis.nra.co.za/portal/mobile/rrm_app.apk"
        val syncDialog: AlertDialog.Builder =
            AlertDialog.Builder(this)
                .setTitle("New Version Available")
                .setMessage(response.errorMessage + getString(R.string.new_line) + getString(R.string.new_line) + "Release Date " + response.releaseDate)
                .setCancelable(false)
                .setIcon(R.drawable.ic_warning_yellow)
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


    private fun startPermissionRequest(permissions: Array<String>): Boolean {
        var allAccess = true
        for (i in permissions.indices) {
            if (checkCallingOrSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                allAccess = false
            }
        }
        return allAccess
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return false
    }

    private fun googlePlayServicesCheck(activity: Activity) {
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity.applicationContext)
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