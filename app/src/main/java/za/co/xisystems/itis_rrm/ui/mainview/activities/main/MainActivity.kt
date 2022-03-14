/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 23:58
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.ui.mainview.activities.main

import am.appwise.components.ni.NoInternetDialog
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.github.ajalt.timberkt.Timber
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.kodein.di.instance
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.constants.Constants.TWO_SECONDS
import za.co.xisystems.itis_rrm.databinding.ActivityMainBinding
import za.co.xisystems.itis_rrm.delegates.viewBinding
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.JobCreationActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.JobCreationActivity.Companion.FRAGMNT
import za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.JobCreationActivity.Companion.FRAG_ID
import za.co.xisystems.itis_rrm.ui.mainview.activities.settings.SettingsActivity
import za.co.xisystems.itis_rrm.ui.mainview.home.HomeFragmentDirections
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.hideKeyboard
import za.co.xisystems.itis_rrm.utils.toast

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val factory: MainActivityViewModelFactory by instance()
    private lateinit var navController: NavController
    private lateinit var navHostFragment: NavHostFragment
    private var toggle: ActionBarDrawerToggle? = null
    private lateinit var navigationView: NavigationView
    private var badgeUnSubmitted: TextView? = null
    private var badgeWork: TextView? = null
    private var badgeApproveJobs: TextView? = null
    private var badgeEstMeasure: TextView? = null
    private var badgeApprovMeasure: TextView? = null
    private lateinit var lm: LocationManager
//    private var gpsEnabled = false
    private var networkEnabled = false
    private var uiScope = UiLifecycleScope()
    private var doubleBackToExitPressed = 0
    private val binding by viewBinding(ActivityMainBinding::inflate)

    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.nav_home
            ),
            binding.drawerLayout
        )
    }

    private var permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_WIFI_STATE,
    )
    companion object {
        private const val PERMISSION_REQUEST = 42
        const val PROJECT_USER_ROLE_IDENTIFIER =
            "RRM Job Mobile User" // "29DB5C213D034EDB88DEC54109EE1711"
        const val PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER =
            "RRM Job Mobile - Site Engineer" // "3F9A15DF5D464EC5A5D954134A7F32BE"
        const val PROJECT_ENGINEER_ROLE_IDENTIFIER =
            "RRM Job Mobile - Engineer" // "D9E16C2A31FA4CC28961E20B652B292C"
        const val PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER =
            "RRM Job Mobile - Sub Contractor" // "03F493BDD6D68944A94BE038B6C1C3D2"

        // "F836F6BF14404E749E6748A31A0262AD"
        const val PROJECT_CONTRACTOR_ROLE_IDENTIFIER =
            "RRM Job Mobile - Contractor" // "E398A3EF1C18431DBAEE4A4AC5D6F07D"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Get a support ActionBar corresponding to this toolbar
        val ab: ActionBar? = supportActionBar
        val noInternetDialog: NoInternetDialog = NoInternetDialog.Builder(this@MainActivity).build()
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true)
        } else {
            throw NullPointerException("Something went wrong")
        }
        getInternetConnectionResult(noInternetDialog)
        // Set MotionToast to use Sanral colours
        MotionToast.setErrorColor(R.color.sanral_dark_red)
        MotionToast.setSuccessColor(R.color.sanral_dark_green)
        MotionToast.setWarningColor(R.color.sanral_dark_red)
        MotionToast.setInfoColor(R.color.dark_bg_color)
        MotionToast.setDeleteColor(R.color.dark_bg_color)

        // Because we're creating the NavHostFragment using FragmentContainerView, we must
        // retrieve the NavController directly from the NavHostFragment instead
        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        navigationView = binding.navView
        NavigationUI.setupActionBarWithNavController(this, navController)
        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfiguration)


        this.mainActivityViewModel = ViewModelProvider(this, factory)[MainActivityViewModel::class.java]

        initializeCountDrawer()

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getUserRoles()
        displayPromptForEnablingGPS(this)
        this.toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle!!)

        toggle!!.syncState()

        this.hideKeyboard()

        binding.navView.setNavigationItemSelectedListener(this)
        badgeUnSubmitted =
            navigationView.menu.findItem(R.id.nav_unSubmitted).actionView as TextView
//        nav_correction =
//            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_correction)) as TextView
        badgeWork =
            navigationView.menu.findItem(R.id.nav_work).actionView as TextView
        badgeApproveJobs =
            navigationView.menu.findItem(R.id.nav_approveJobs).actionView as TextView
        badgeApprovMeasure =
            navigationView.menu.findItem(R.id.nav_approveMeasure).actionView as TextView
        badgeEstMeasure =
            navigationView.menu.findItem(R.id.nav_estMeasure).actionView as TextView
    }

    private fun getInternetConnectionResult(noInternetDialog: NoInternetDialog) {
        if (!startPermissionRequest(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST)
            Snackbar.make(
                this@MainActivity,
                binding.root, "Error Permissions Missing, Check and Allow!!",
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.retry), View.OnClickListener
            // Handle the Retry Button Click
            {
                // Restart MyTask after 3 seconds
                getInternetConnectionResult(noInternetDialog)
            }).show()
        }else{
            if (!ServiceUtil.isNetworkConnected(this@MainActivity)) {
                noInternetDialog.showDialog()
                Snackbar.make(
                    this@MainActivity,
                    binding.root, "Error Service Host Un-Reachable Check Network And Data Connection!!",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(getString(R.string.retry), View.OnClickListener
                // Handle the Retry Button Click
                {
                    // Restart MyTask after 3 seconds
                    getInternetConnectionResult(noInternetDialog)
                }).show()
            } else {
                if (noInternetDialog.isShowing) {
//                    binding.progressBar.visibility = View.GONE
                } else {
                    //acquireUser(noInternetDialog)
                }
            }
        }
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

    private fun displayPromptForEnablingGPS(
        activity: Activity
    ) {
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            Timber.e(e, { "GPS-related error" })
        }

        try {
            networkEnabled = ServiceUtil.isNetworkAvailable(applicationContext)
        } catch (e: Exception) {
            Timber.e(e, { "Network-related error" })
        }

        if (!gpsEnabled) { // notify user

            promptGpsActivation(activity)
        }
    }

    private fun promptGpsActivation(activity: Activity) {
        val builder = AlertDialog.Builder(this)
        val action = Settings.ACTION_LOCATION_SOURCE_SETTINGS
        val message = ("Your GPS seems to be disabled, Please enable it to continue")
        builder.setMessage(message)
            .setPositiveButton(
                "OK"
            ) { d, _ ->
                activity.startActivity(Intent(action))
                d.dismiss()
            }
        builder.create().show()
    }

    override fun onBackPressed() {

        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (navHostFragment.childFragmentManager.backStackEntryCount > 1) {
                super.onBackPressed()
                toggle?.syncState()
            } else {
                doubleBackToExitPressed++
                if (doubleBackToExitPressed == 2) {
                    logoutApplication()
                    finishAndRemoveTask()
                } else {

                    toast("Please press Back again to exit")

                    Handler(mainLooper).postDelayed({
                        doubleBackToExitPressed = 0
                    }, TWO_SECONDS)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        val item = menu.findItem(R.id.action_settings)

        item.isVisible = true

        val searchItem = menu.findItem(R.id.action_search)
        searchItem.isVisible = false
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                // on submit send entire query
                return false
            }
        })
        searchView.queryHint = "Search "
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_search -> {
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logoutApplication()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {

        return NavigationUI.navigateUp(navController, binding.drawerLayout) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val home = HomeFragmentDirections.actionGlobalNavHome()

        when (item.itemId) {
            R.id.nav_home -> {
//                navController.navigate(home)
//                toggle?.syncState()
//                initializeCountDrawer()
                Intent(this, MainActivity::class.java).also { home ->
                    toggle?.syncState()
                    initializeCountDrawer()
                    home.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
            }
            R.id.nav_create -> {
                toggle?.syncState()
                Intent(this, JobCreationActivity::class.java).also { create ->
                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    create.putExtra(FRAG_ID, "navigation_create")
                    create.putExtra(FRAGMNT, true)
                    startActivity(create)
                }
            }

            R.id.nav_unSubmitted -> {
                toggle?.syncState()
                Intent(this, JobCreationActivity::class.java).also { create ->
                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    create.putExtra(FRAG_ID, "navigation_un_submitted")
                    create.putExtra(FRAGMNT, true)
                    startActivity(create)
                }
            }
            R.id.nav_correction -> {
                toggle?.syncState()
                Intent(this, JobCreationActivity::class.java).also { create ->
                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    create.putExtra(FRAG_ID, "navigation_correction")
                    create.putExtra(FRAGMNT, true)
                    startActivity(create)
                }
            }
            R.id.nav_work -> {
//               val unsubDirections = HomeFragmentDirections.actionNavHomeToNavUnSubmitted()
//                navController.navigate(home)
//                navController.navigate(unsubDirections)
                toggle?.syncState()
                Intent(this, JobCreationActivity::class.java).also { create ->
                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    create.putExtra(FRAG_ID, "navigation_work")
                    create.putExtra(FRAGMNT, true)
                    startActivity(create)
                }
            }
            R.id.nav_estMeasure -> {
                toggle?.syncState()
                Intent(this, JobCreationActivity::class.java).also { create ->
                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    create.putExtra(FRAG_ID, "navigation_measure_est")
                    create.putExtra(FRAGMNT, true)
                    startActivity(create)
                }
            }
            R.id.nav_approveJobs -> {
//                Intent(this, JobCreationActivity::class.java).also { create ->
//                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(create)
//                }
                val approveDirections = HomeFragmentDirections.actionNavHomeToNavApproveJobs()
                navController.navigate(home)
                navController.navigate(approveDirections)
                toggle?.syncState()
            }
            R.id.nav_approveMeasure -> {
//                Intent(this, JobCreationActivity::class.java).also { create ->
//                    create.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//                    startActivity(create)
//                }
                val approveMeasureDirections = HomeFragmentDirections.actionNavHomeToNavApproveMeasure()
                navController.navigate(home)
                navController.navigate(approveMeasureDirections)
                toggle?.syncState()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    // Control Menu drawer View Access Based on who is logged in
    private fun getUserRoles() {
        Coroutines.main {
            // Initially disable all menu options
            val menuNav = navigationView.menu

            val navCreate = menuNav.findItem(R.id.nav_create)
            navCreate.isEnabled = false

            val navUnsubmitted = menuNav.findItem(R.id.nav_unSubmitted)
            navUnsubmitted.isEnabled = false

            // NB: Corrections are for phase 2

            val navWork = menuNav.findItem(R.id.nav_work)
            navWork.isEnabled = false

            val navApproveJobs = menuNav.findItem(R.id.nav_approveJobs)
            navApproveJobs.isEnabled = false

            val navEstMeasures = menuNav.findItem(R.id.nav_estMeasure)
            navEstMeasures.isEnabled = false

            val navApproveMeasures = menuNav.findItem(R.id.nav_approveMeasure)
            navApproveMeasures.isEnabled = false

            val userRoles = mainActivityViewModel.getRoles()
            userRoles.observe(this, { roleList ->

                for (role in roleList) {
                    val roleID = role.roleDescription
                    // Only enable what is needed for each role description.
                    // Users with multiple roles get 'best permissions'
                    when {
                        roleID.equals(PROJECT_USER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navUnsubmitted.isEnabled = true
                            navWork.isVisible = false
                            navApproveMeasures.isVisible = false
                            navApproveJobs.isVisible = false
                            navEstMeasures.isVisible = false
                        }

                        roleID.equals(PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navWork.isEnabled = true
                            navApproveMeasures.isVisible = false
                            navApproveJobs.isVisible = false
                            navCreate.isVisible = false
                            navUnsubmitted.isVisible = false
                            navEstMeasures.isVisible = false
                        }

                        roleID.equals(PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navUnsubmitted.isEnabled = true
                            navWork.isEnabled = true
                            navEstMeasures.isEnabled = true
                            navEstMeasures.isVisible = true
                            navWork.isVisible = true
                            navApproveMeasures.isVisible = false
                            navApproveJobs.isVisible = false

                        }

                        roleID.equals(PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navUnsubmitted.isEnabled = true
                            navEstMeasures.isEnabled = true

                            navWork.isVisible = false
                            navApproveMeasures.isVisible = false
                            navApproveJobs.isVisible = false

                        }

                        roleID.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navCreate.isVisible = true

                            navUnsubmitted.isEnabled = true
                            navUnsubmitted.isVisible = true

                            navWork.isVisible = false

                            navApproveJobs.isEnabled = true
                            navApproveJobs.isVisible = true

                            navEstMeasures.isEnabled = true
                            navEstMeasures.isVisible = true

                            navApproveMeasures.isEnabled = true
                            navApproveMeasures.isVisible = true

                        }
                    }
                }
            })
        }
    }

    private fun initializeCountDrawer() {
        // Estimates are completed needs to be submitted currently saved in the local DB

        uiScope.launch(uiScope.coroutineContext) {

            // Unsubmitted jobs are collected here
            mainActivityViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_ESTIMATE,
                ActivityIdConstants.JOB_PENDING_UPLOAD
            ).observe(this@MainActivity, { newJobData ->
                val tasks = newJobData.distinctBy { job -> job.jobId }.count()
                writeBadge(badgeUnSubmitted, tasks)
            })

            // Corrections section is for Phase 2

            // Estimates are approved and work can start
            mainActivityViewModel.getJobsForActivityId2(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            ).observe(this@MainActivity, { workList ->
                val tasks = workList.distinctBy { job -> job.jobId }.count()
                writeBadge(badgeWork, tasks)
            })

            // Estimate measurements
            mainActivityViewModel.getJobMeasureForActivityId(
                ActivityIdConstants.ESTIMATE_MEASURE,
                ActivityIdConstants.JOB_ESTIMATE,
                ActivityIdConstants.MEASURE_PART_COMPLETE
            ).observe(this@MainActivity, { measurementJobs ->
                val tasks = measurementJobs.distinctBy { job -> job.jobId }.count()
                writeBadge(badgeEstMeasure, tasks)
            })

            // Jobs awaiting approval
            mainActivityViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVE
            ).observe(this@MainActivity, { jobApprovalData ->
                val tasks = jobApprovalData.count()
                writeBadge(badgeApproveJobs, tasks)
            })

            // Measurements are completed needs approval for payment
            mainActivityViewModel.getJobApproveMeasureForActivityId(
                ActivityIdConstants.MEASURE_COMPLETE
            ).observe(this@MainActivity, {
                val tasks = it.distinctBy { job -> job.jobId }.count()
                writeBadge(badgeApprovMeasure, tasks)
            })
        }
    }

    override fun startLongRunningTask() {
        Timber.e{"starting task..."}
        binding.progressbar.visibility = View.VISIBLE
        // Make UI untouchable for duration of task
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    override fun endLongRunningTask() {
        Timber.i { "stopping task..." }
        binding.progressbar.visibility = View.GONE
        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // Common routine to create badges
    private fun writeBadge(textView: TextView?, tasks: Int = 0) {
        if (tasks == 0) {
            textView?.text = ""
        } else {
            textView?.gravity = Gravity.CENTER_VERTICAL
            textView?.setTypeface(null, Typeface.BOLD)
            textView?.setTextColor(
                ContextCompat.getColor(applicationContext, R.color.red)
            )
            textView?.text = this.getString(R.string.badge, tasks.toString())
        }
    }

    @Suppress("ControlFlowWithEmptyBody")
    override fun onResume() {
        super.onResume()
        if (this.delegate.localNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Restore the theme? Implement dark material theme
            // HOME.equals(true)
            // initializeCountDrawer()
            // refreshData()
        }
    }
}
