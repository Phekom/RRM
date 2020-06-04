package za.co.xisystems.itis_rrm

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.raygun.raygun4android.RaygunClient
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import timber.log.Timber
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.*
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.hideKeyboard


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    KodeinAware {

    override val kodein by kodein()
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val factory: MainActivityViewModelFactory by instance<MainActivityViewModelFactory>()
    private lateinit var sharedViewModel: SharedViewModel
    private val shareFactory: SharedViewModelFactory by instance<SharedViewModelFactory>()
    var navController: NavController? = null
    var toggle: ActionBarDrawerToggle? = null
    lateinit var navigationView: NavigationView
    var qty = 0
    var nav_unSubmitted: TextView? = null
    var nav_work: TextView? = null
    var nav_approveJbs: TextView? = null
    var nav_estMeasure: TextView? = null
    var nav_approvMeasure: TextView? = null

    lateinit var lm: LocationManager
    var gps_enabled = false
    var network_enabled = false

    var progressBar: ProgressBar? = null

    val PROJECT_USER_ROLE_IDENTIFIER = "RRM Job Mobile User" //"29DB5C213D034EDB88DEC54109EE1711"
    val PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER =
        "RRM Job Mobile - Site Engineer" //"3F9A15DF5D464EC5A5D954134A7F32BE"
    val PROJECT_ENGINEER_ROLE_IDENTIFIER =
        "RRM Job Mobile - Engineer" //"D9E16C2A31FA4CC28961E20B652B292C"
    val PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER =
        "RRM Job Mobile - Sub Contractor" //"03F493BDD6D68944A94BE038B6C1C3D2" //"F836F6BF14404E749E6748A31A0262AD"
    val PROJECT_CONTRACTOR_ROLE_IDENTIFIER =
        "RRM Job Mobile - Contractor"  //"E398A3EF1C18431DBAEE4A4AC5D6F07D"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        RaygunClient.init(application)
        RaygunClient.enableCrashReporting()

        this.mainActivityViewModel = this.run {
            ViewModelProvider(this, factory).get(MainActivityViewModel::class.java)
        }

        this.sharedViewModel = this.run {
            ViewModelProvider(this, shareFactory).get(SharedViewModel::class.java)
        }

        initializeCountDrawer()

        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        this.delegate.localNightMode.equals( AppCompatDelegate.MODE_NIGHT_YES)
//        (this@MainActivity as MainActivity?)?.delegate?.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
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
        navigationView = findViewById(R.id.nav_view)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController!!)
        NavigationUI.setupWithNavController(navigationView, navController!!)

        nav_view.setNavigationItemSelectedListener(this)
        nav_unSubmitted =
            navigationView.menu.findItem(R.id.nav_unSubmitted).actionView as TextView
//        nav_correction =
//            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_correction)) as TextView
        nav_work =
            navigationView.menu.findItem(R.id.nav_work).actionView as TextView
        nav_approveJbs =
            navigationView.menu.findItem(R.id.nav_approveJbs).actionView as TextView
        nav_approvMeasure =
            navigationView.menu.findItem(R.id.nav_approvMeasure).actionView as TextView
        nav_estMeasure =
            navigationView.menu.findItem(R.id.nav_estMeasure).actionView as TextView
        progressBar = findViewById(R.id.progressbar)

        sharedViewModel.longRunning.observe(this, Observer {
            when (it) {
                true -> this.startLongRunningTask()
                false -> this.endLongRunningTask()
            }
        })

        sharedViewModel.message.observe(this, Observer {
            toastMessage(it.toString())
        })

        sharedViewModel.actionCaption.observe(this@MainActivity, Observer {
            setCaption(it)
        })
    }


    fun displayPromptForEnablingGPS(
        activity: Activity
    ) {
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!gps_enabled && !network_enabled) { // notify user

            val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
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
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                // task HERE
                //on submit send entire query
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


//                startActivity(Intent(this, SettingsActivity::class.java))
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
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawer_layout) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
                navController?.navigate(R.id.nav_home)
                toggle?.syncState()
                initializeCountDrawer()
            }
            R.id.nav_create -> {
                navController?.navigate(R.id.nav_create)
//                ActivityFactory.startNewJobActivity(this)
                toggle?.syncState()
//                supportActionBar?.setTitle("New JobDTO")
//                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            }
            R.id.nav_unSubmitted -> {
                navController?.navigate(R.id.nav_unSubmitted)
                toggle?.syncState()

            }
            R.id.nav_correction -> {
                navController?.navigate(R.id.nav_correction)
                toggle?.syncState()
            }
            R.id.nav_work -> {
                navController?.navigate(R.id.nav_work)
                toggle?.syncState()
            }
            R.id.nav_estMeasure -> {
                navController?.navigate(R.id.nav_estMeasure)
                toggle?.syncState()
            }
            R.id.nav_approveJbs -> {
                navController?.navigate(R.id.nav_approveJbs)
                toggle?.syncState()
            }
            R.id.nav_approvMeasure -> {
                navController?.navigate(R.id.nav_approvMeasure)
                toggle?.syncState()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun startLongRunningTask() {
        Timber.i("starting task...")
        progressBar?.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun endLongRunningTask() {
        Timber.i("stopping task...")
        progressBar?.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun toastMessage(message: String) {
        ToastUtils().toastLong(this.applicationContext, message)
    }

    fun setCaption(caption: String) {
        sharedViewModel.originalCaption = supportActionBar?.title.toString()
        supportActionBar?.title = caption
    }

    // Control Menu drawer View Access Based on who is logged in
    fun getUserRoles() {
        Coroutines.main {
            val userRoles = mainActivityViewModel.getRoles()
            userRoles.observe(this, Observer { roleList ->
                val menuNav = navigationView.menu
                var navItem: MenuItem

                for (role in roleList) {
                    val roleID = role.roleDescription

                    if (roleID.equals(PROJECT_USER_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        navItem = menuNav.findItem(R.id.nav_create)
                        navItem.isEnabled = true

                        navItem = menuNav.findItem(R.id.nav_unSubmitted)
                        navItem.isEnabled = true

//                        nav_item = menuNav.findItem(R.id.nav_correction)
//                        nav_item.isEnabled = false

                        navItem = menuNav.findItem(R.id.nav_work)
                        navItem.isEnabled = false

                        navItem = menuNav.findItem(R.id.nav_approveJbs)
                        navItem.isEnabled = false

                        navItem = menuNav.findItem(R.id.nav_estMeasure)
                        navItem.isEnabled = false

                        navItem = menuNav.findItem(R.id.nav_approvMeasure)
                        navItem.isEnabled = false


                    }

                    if (roleID.equals(PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val navItem1 = menuNav.findItem(R.id.nav_create)
                        navItem1.isEnabled = false

                        val navItem2 = menuNav.findItem(R.id.nav_unSubmitted)
                        navItem2.isEnabled = false

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val navItem4 = menuNav.findItem(R.id.nav_work)
                        navItem4.isEnabled = true

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.isEnabled = false

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.isEnabled = false

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.isEnabled = false


                    }

                    if (roleID.equals(PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.isEnabled = true

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.isEnabled = true

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.isEnabled = true

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.isEnabled = false

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.isEnabled = true

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.isEnabled = false


                    }

                    if (roleID.equals(PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.isEnabled = true

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.isEnabled = true

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.isEnabled = false

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.isEnabled = false

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.isEnabled = true

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.isEnabled = false


                    }

                    if (roleID.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.isEnabled = true

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.isEnabled = true

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.isEnabled = false

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.isEnabled = true

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.isEnabled = true

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.isEnabled = true


                    }
                }
            })

        }


    }


    fun initializeCountDrawer() {
        // Estimates are completed needs to be submitted currently saved in the local DB

        Coroutines.main {
            val new_job = mainActivityViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_ESTIMATE
            )
            new_job.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
//            qty = 50
                if (qty == 0) {
                    nav_unSubmitted?.text = ""
                } else {
                    nav_unSubmitted?.gravity = Gravity.CENTER_VERTICAL
                    nav_unSubmitted?.setTypeface(null, Typeface.BOLD)
                    nav_unSubmitted?.setTextColor(
                        ContextCompat.getColor(applicationContext, R.color.red)
                    )
                    nav_unSubmitted?.text = "( $qty )"
                }
            })

//        //====================================================================================================================================================
// Estimates are completed needs Correction for approval
            //        CorrectionsFragment.addEntitiesToJobArrayList(
//                jobDataController,
//                toDoListDataController.getEntitiesForActivityId(ActivityIdConstants.JOB_APPROVE),
//                jobArrayList1);
//        qty = jobDataController.getJobsForActivityId(ActivityIdConstants.JOB_FAILED).size
//        if (qty == 0) {
//        } else {
//            nav_correction?.setGravity(Gravity.CENTER_VERTICAL)
//            nav_correction?.setTypeface(null, Typeface.BOLD)
//            nav_correction?.setTextColor(resources.getColor(R.color.red))
//            nav_correction?.setText("( $qty )")
//        }
//        //====================================================================================================================================================
//// Estimates are approved and work can start
            val work = mainActivityViewModel.getJobsForActivityId2(
                ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE
            )
            work.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_work?.text = ""
                } else {
                    nav_work?.gravity = Gravity.CENTER_VERTICAL
                    nav_work?.setTypeface(null, Typeface.BOLD)
                    nav_work?.setTextColor(ContextCompat.getColor(applicationContext, R.color.red))
                    nav_work?.text = "( $qty )"
                }
            })
//        //====================================================================================================================================================
//            val measurements = mainActivityViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE, ActivityIdConstants.JOB_ESTIMATE)
            val measurements = mainActivityViewModel.getJobMeasureForActivityId(
                ActivityIdConstants.ESTIMATE_MEASURE,
                ActivityIdConstants.JOB_ESTIMATE,
                ActivityIdConstants.MEASURE_PART_COMPLETE
            )
//            val measurements = mainActivityViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE)
            measurements.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_estMeasure?.text = ""
                } else {
                    nav_estMeasure?.gravity = Gravity.CENTER_VERTICAL
                    nav_estMeasure?.setTypeface(null, Typeface.BOLD)
                    nav_estMeasure?.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.red
                        )
                    )
                    nav_estMeasure?.text = "( $qty )"
                }
            })
//            nav_estMeasure.setText("( $qty )")
//        }
//        //===================================================================================================================================================\

            val j_approval = mainActivityViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVE
            )
            j_approval.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_approveJbs?.text = ""
                } else {
                    nav_approveJbs?.gravity = Gravity.CENTER_VERTICAL
                    nav_approveJbs?.setTypeface(null, Typeface.BOLD)
                    nav_approveJbs?.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.red
                        )
                    )
                    nav_approveJbs?.text = "( $qty )"
                }
            })
//        //=====================================================================================================================================================
            // Measurements are completed needs approval for payment
            val m_approval =
                mainActivityViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)
            m_approval.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_approvMeasure?.text = ""
                } else {
                    nav_approvMeasure?.gravity = Gravity.CENTER_VERTICAL
                    nav_approvMeasure?.setTypeface(null, Typeface.BOLD)
                    nav_approvMeasure?.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.red
                        )
                    )
                    nav_approvMeasure?.text = "( $qty )"
                }
            })

        }
    }

    override fun onResume() {
        super.onResume()
        if (this.delegate.localNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            // TODO: Restore the theme?
            // HOME.equals(true)
            // initializeCountDrawer()
            // refreshData()
        }
    }


    companion object {
        private val TAG = MainActivity::class.java.simpleName
        var switch: Switch? = null

    }


}
