/*
 * Updated by Shaun McDonald on 2021/01/25
 * Last modified on 2021/01/25 6:30 PM
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */


package za.co.xisystems.itis_rrm

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.raygun.raygun4android.RaygunClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import za.co.xisystems.itis_rrm.custom.notifications.ColorToast
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.SharedViewModelFactory
import za.co.xisystems.itis_rrm.ui.scopes.UiLifecycleScope
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.ServiceUtil
import za.co.xisystems.itis_rrm.utils.hideKeyboard

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    KodeinAware {

    override val kodein by kodein()
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val factory: MainActivityViewModelFactory by instance()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance()
    private lateinit var navController: NavController
    private var toggle: ActionBarDrawerToggle? = null
    private lateinit var navigationView: NavigationView
    private var badgeUnSubmitted: TextView? = null
    private var badgeWork: TextView? = null
    private var badgeApproveJobs: TextView? = null
    private var badgeEstMeasure: TextView? = null
    private var badgeApprovMeasure: TextView? = null

    private lateinit var lm: LocationManager
    private var gpsEnabled = false
    private var networkEnabled = false

    private var progressBar: ProgressBar? = null
    private var uiScope = UiLifecycleScope()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Because we're creating the NavHostFragment using FragmentContainerView, we must
        // retrieve the NavController directly from the NavHostFragment instead
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        navigationView = findViewById(R.id.nav_view)
        NavigationUI.setupActionBarWithNavController(this, navController)
        NavigationUI.setupWithNavController(navigationView, navController)

        RaygunClient.init(application)
        RaygunClient.enableCrashReporting()

        // Set MotionToast to use Sanral colours
        MotionToast.setErrorColor(R.color.sanral_dark_red)
        MotionToast.setSuccessColor(R.color.sanral_dark_green)
        MotionToast.setWarningColor(R.color.colorPrimaryYellow)
        MotionToast.setInfoColor(R.color.dark_bg_color)
        MotionToast.setDeleteColor(R.color.sanral_orange_red)

        this.mainActivityViewModel = this.run {
            ViewModelProvider(this, factory).get(MainActivityViewModel::class.java)
        }

        this.sharedViewModel = this.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        }

        initializeCountDrawer()

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        getUserRoles()
        displayPromptForEnablingGPS(this)
        this.toggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawer_layout.addDrawerListener(toggle!!)

        toggle!!.syncState()

        this.hideKeyboard()

        nav_view.setNavigationItemSelectedListener(this)
        badgeUnSubmitted =
            navigationView.menu.findItem(R.id.nav_unSubmitted).actionView as TextView
//        nav_correction =
//            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_correction)) as TextView
        badgeWork =
            navigationView.menu.findItem(R.id.nav_work).actionView as TextView
        badgeApproveJobs =
            navigationView.menu.findItem(R.id.nav_approveJbs).actionView as TextView
        badgeApprovMeasure =
            navigationView.menu.findItem(R.id.nav_approvMeasure).actionView as TextView
        badgeEstMeasure =
            navigationView.menu.findItem(R.id.nav_estMeasure).actionView as TextView
        progressBar = findViewById(R.id.progressbar)

        sharedViewModel.longRunning.observe(this, {
            when (it) {
                true -> this.startLongRunningTask()
                false -> this.endLongRunningTask()
            }
        })

        sharedViewModel.message.observe(this, {
            toastMessage(it.toString())
        })

        sharedViewModel.colorMessage.observe(this, {
            it?.let {
                toastMessage(it)
            }
        })

        sharedViewModel.actionCaption.observe(this@MainActivity, {
            setCaption(it)
        })
    }

    private fun displayPromptForEnablingGPS(
        activity: Activity
    ) {
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            networkEnabled = ServiceUtil.isNetworkAvailable(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
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
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            toggle?.syncState()
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
        when (item.itemId) {
            R.id.action_search -> {

                return true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_logout -> {
                Intent(this, LoginActivity::class.java).also { home ->
                    home.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(home)
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_container)
        return NavigationUI.navigateUp(navController, drawer_layout) // ?:  navController.navigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        when (item.itemId) {
            R.id.nav_home -> {
                navController.navigate(R.id.nav_home)
                toggle?.syncState()
                initializeCountDrawer()
            }
            R.id.nav_create -> {
                navController.navigate(R.id.nav_create)
                toggle?.syncState()
            }

            R.id.nav_unSubmitted -> {
                navController.navigate(R.id.nav_unSubmitted)
                toggle?.syncState()
            }
            R.id.nav_correction -> {
                navController.navigate(R.id.nav_correction)
                toggle?.syncState()
            }
            R.id.nav_work -> {
                navController.navigate(R.id.nav_work)
                toggle?.syncState()
            }
            R.id.nav_estMeasure -> {
                navController.navigate(R.id.nav_estMeasure)
                toggle?.syncState()
            }
            R.id.nav_approveJbs -> {
                navController.navigate(R.id.nav_approveJbs)
                toggle?.syncState()
            }
            R.id.nav_approvMeasure -> {
                navController.navigate(R.id.nav_approvMeasure)
                toggle?.syncState()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun startLongRunningTask() {
        Timber.i("starting task...")
        progressBar?.visibility = View.VISIBLE

        // Make UI untouchable for duration of big synch
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    private fun endLongRunningTask() {
        Timber.i("stopping task...")
        progressBar?.visibility = View.GONE

        // Re-enable UI touches
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun toastMessage(message: String) {
        ToastUtils().toastShort(applicationContext, message)
    }

    private fun setCaption(caption: String) {
        sharedViewModel.originalCaption = supportActionBar?.title.toString()
        supportActionBar?.title = caption
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

            val navApproveJobs = menuNav.findItem(R.id.nav_approveJbs)
            navApproveJobs.isEnabled = false

            val navEstMeasures = menuNav.findItem(R.id.nav_estMeasure)
            navEstMeasures.isEnabled = false

            val navApproveMeasures = menuNav.findItem(R.id.nav_approvMeasure)
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
                        }

                        roleID.equals(PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {

                            navWork.isEnabled = true
                        }

                        roleID.equals(PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {

                            navCreate.isEnabled = true
                            navUnsubmitted.isEnabled = true
                            navWork.isEnabled = true
                            navEstMeasures.isEnabled = true
                        }

                        roleID.equals(PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {

                            navCreate.isEnabled = true
                            navUnsubmitted.isEnabled = true
                            navEstMeasures.isEnabled = true
                        }

                        roleID.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navUnsubmitted.isEnabled = true
                            navWork.isEnabled = true
                            navEstMeasures.isEnabled = true
                            navApproveMeasures.isEnabled = true
                            navApproveJobs.isEnabled = true
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
                ActivityIdConstants.JOB_ESTIMATE
            ).observe(this@MainActivity, { newJobData ->
                val tasks = newJobData.distinctBy { job -> job.jiNo }.count()
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

    private fun toastMessage(
        colorToast: ColorToast
    ) {
        MotionToast.createColorToast(
            context = this,
            title = colorToast.title,
            message = colorToast.message,
            style = colorToast.style.getValue(),
            position = colorToast.gravity.getValue(),
            duration = colorToast.duration.getValue(),
            font = ResourcesCompat.getFont(this, R.font.helvetica_regular)
        )
    }

    override fun onResume() {
        super.onResume()
        if (this.delegate.localNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // Restore the theme? Implement dark material theme
            // HOME.equals(true)
            // initializeCountDrawer()
            // refreshData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        uiScope.destroy()
    }

    companion object {
        const val PROJECT_USER_ROLE_IDENTIFIER = "RRM Job Mobile User" // "29DB5C213D034EDB88DEC54109EE1711"
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
}
