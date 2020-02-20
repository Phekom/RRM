package za.co.xisystems.itis_rrm

//import com.google.android.gms.appindexing.AppIndex

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.raygun.raygun4android.RaygunClient
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.MainActivityViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsActivity.Companion.HOME
import za.co.xisystems.itis_rrm.utils.ActivityIdConstants
import za.co.xisystems.itis_rrm.utils.Coroutines
import za.co.xisystems.itis_rrm.utils.hideKeyboard


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener , KodeinAware {



    override val kodein by kodein()
    private lateinit var mainActivityViewModel: MainActivityViewModel
    private val factory: MainActivityViewModelFactory by instance()

    var navController: NavController? = null
    var toggle : ActionBarDrawerToggle? = null
    lateinit var navigationView: NavigationView
    var qty = 0
    var nav_unSubmitted: TextView? = null
    var nav_correction:TextView? = null
    var nav_work:TextView? = null
    var nav_approveJbs:TextView? = null
    var nav_estMeasure:TextView? = null
    var nav_approvMeasure:TextView? = null

    lateinit var lm : LocationManager
    var gps_enabled = false
    var network_enabled = false


    val PROJECT_USER_ROLE_IDENTIFIER = "29DB5C213D034EDB88DEC54109EE1711"
    val PROJECT_TESTER_ROLE_IDENTIFIER = "CEE622559D0640BFAE98E39BA4917AF3"
    val PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER = "3F9A15DF5D464EC5A5D954134A7F32BE"
    val PROJECT_ENGINEER_ROLE_IDENTIFIER = "D9E16C2A31FA4CC28961E20B652B292C"
    val PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER = "E398A3EF1C18431DBAEE4A4AC5D6F07D"
    val PROJECT_CONTRACTOR_ROLE_IDENTIFIER = "E398A3EF1C18431DBAEE4A4AC5D6F07D"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        RaygunClient.init(application);
        RaygunClient.enableCrashReporting();

        mainActivityViewModel = this?.run {
            ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)}
        initializeCountDrawer()
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        this.delegate.localNightMode.equals( AppCompatDelegate.MODE_NIGHT_YES)
//        (this@MainActivity as MainActivity?)?.delegate?.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
        getUseRoles()
        checkGPSEnabled()
        this.toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle!!)
        toggle!!.syncState()
        this.hideKeyboard()
        navigationView  = findViewById(R.id.nav_view)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)
        NavigationUI.setupActionBarWithNavController(this, navController!!)
        NavigationUI.setupWithNavController(navigationView, navController!!)

        nav_view.setNavigationItemSelectedListener(this)
        nav_unSubmitted =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_unSubmitted)) as TextView
//        nav_correction =
//            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_correction)) as TextView
        nav_work =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_work)) as TextView
        nav_approveJbs =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_approveJbs)) as TextView
        nav_approvMeasure =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_approvMeasure)) as TextView
        nav_estMeasure =
            MenuItemCompat.getActionView(navigationView.menu.findItem(R.id.nav_estMeasure)) as TextView
    }

    private fun checkGPSEnabled() {
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
            AlertDialog.Builder(this)
                .setMessage("Please turn on Location to continue")
                .setCancelable(false)
                .setPositiveButton(
                    "Open Location Settings",
                    DialogInterface.OnClickListener { paramDialogInterface, paramInt ->
                        startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                    })
                .setNegativeButton("Cancel", null)
                .show()
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
        searchView.setOnQueryTextListener(object :  SearchView.OnQueryTextListener {

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
            R.id.action_search ->{




//                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_settings ->{
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_logout ->{
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


    // Control Menu drawer View Access Based on who is logged in
    fun getUseRoles() {
        Coroutines.main {
           val useroles = mainActivityViewModel.getRoles()
            useroles.observe(this, Observer {roleList ->
                val menuNav = navigationView.getMenu()
                var nav_item: MenuItem

                for (role in roleList) {
                    val IDss = role.roleIdentifier

                    if (IDss.equals(PROJECT_USER_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        nav_item = menuNav.findItem(R.id.nav_create)
                        nav_item.isEnabled = true

                        nav_item = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item.isEnabled = true

//                        nav_item = menuNav.findItem(R.id.nav_correction)
//                        nav_item.isEnabled = false

                        nav_item = menuNav.findItem(R.id.nav_work)
                        nav_item.isEnabled = false

                        nav_item = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item.isEnabled = false

                        nav_item = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item.isEnabled = false

                        nav_item = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item.isEnabled = false


                    }

                    if (IDss.equals(PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.setEnabled(false)

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.setEnabled(false)

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.setEnabled(true)

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.setEnabled(false)

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.setEnabled(false)

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.setEnabled(false)


                    }

                    if (IDss.equals(PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.setEnabled(true)

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.setEnabled(true)

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.setEnabled(true)

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.setEnabled(false)

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.setEnabled(true)

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.setEnabled(false)


                    }

                    if (IDss.equals(PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.setEnabled(true)

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.setEnabled(true)

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.setEnabled(false)

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.setEnabled(false)

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.setEnabled(true)

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.setEnabled(false)


                    }

                    if (IDss.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true)) {
//                initializeCountDrawer()
                        val nav_item1 = menuNav.findItem(R.id.nav_create)
                        nav_item1.setEnabled(false)

                        val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
                        nav_item2.setEnabled(false)

//                        val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                        nav_item3.setEnabled(false)

                        val nav_item4 = menuNav.findItem(R.id.nav_work)
                        nav_item4.setEnabled(false)

                        val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
                        nav_item5.setEnabled(true)

                        val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
                        nav_item6.setEnabled(false)

                        val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
                        nav_item7.setEnabled(true)


                    }
                }
            })

        }


    }


//    // WebAPI 2.0 Service Call
//    private fun refreshContractInfo() {
//        val contractsViewModel = ContractsViewModel()
//        val messages = arrayOf(
//            getString(R.string.refreshing_contract_related_info) + getString(R.string.new_line) + getString(
//                R.string.please_wait
//            )
//        )
//        val id = registrationInfoDataSource.getUserId()
//        contractsViewModel.refreshContractInfo(
//            id,
//            object : BaseCallBackView<ContractRefreshResponse>(this, messages) {
//                fun processData(response: ContractRefreshResponse) {
//                    if (response.getOfflinedata() != null) {
//                        val offlinedata = response.getOfflinedata()
//                        registerDataController.insertContractRelatedInfo(offlinedata)
//                        // Get the effective roles
//                        refreshEffectiveRoles()
//                    } else {
//                        toast(R.string.refreshing_contract_info_failed)
//                    }
//                }
//            })
//    }


    fun initializeCountDrawer() { // Estimates are completed needs to be submitted currently saved in the local DB
//        val offlineDataRepository: OfflineDataRepository? = null
//        val unsubmittedViewModel = UnSubmittedViewModel(offlineDataRepository!!)
        Coroutines.main {
            //            mydata_loading.show()
            val new_job = mainActivityViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_ESTIMATE)
            new_job.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
//            qty = 50
                if (qty == 0) {
                    nav_unSubmitted?.setText("")
                } else {
                    nav_unSubmitted?.setGravity(Gravity.CENTER_VERTICAL)
                    nav_unSubmitted?.setTypeface(null, Typeface.BOLD)
                    nav_unSubmitted?.setTextColor(resources.getColor(R.color.red))
                    nav_unSubmitted?.setText("( $qty )")
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
            val work = mainActivityViewModel.getJobsForActivityId2(ActivityIdConstants.JOB_APPROVED,
                ActivityIdConstants.ESTIMATE_INCOMPLETE)
            work.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_work?.setText("")
        } else {
            nav_work?.setGravity(Gravity.CENTER_VERTICAL)
            nav_work?.setTypeface(null, Typeface.BOLD)
            nav_work?.setTextColor(resources.getColor(R.color.red))
            nav_work?.setText("( $qty )")
        } })
//        //====================================================================================================================================================
//            val measurements = mainActivityViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE, ActivityIdConstants.JOB_ESTIMATE)
            val measurements = mainActivityViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE, ActivityIdConstants.JOB_ESTIMATE,ActivityIdConstants.MEASURE_PART_COMPLETE)
//            val measurements = mainActivityViewModel.getJobMeasureForActivityId(ActivityIdConstants.ESTIMATE_MEASURE)
            measurements.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_estMeasure?.setText("")
                } else {
                    nav_estMeasure?.setGravity(Gravity.CENTER_VERTICAL)
                    nav_estMeasure?.setTypeface(null, Typeface.BOLD)
                    nav_estMeasure?.setTextColor(resources.getColor(R.color.red))
                    nav_estMeasure?.setText("( $qty )")
                }
            })
//            nav_estMeasure.setText("( $qty )")
//        }
//        //===================================================================================================================================================\

            val j_approval = mainActivityViewModel.getJobsForActivityId(
                ActivityIdConstants.JOB_APPROVE)
            j_approval.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_approveJbs?.setText("")
                } else {
                    nav_approveJbs?.setGravity(Gravity.CENTER_VERTICAL)
                    nav_approveJbs?.setTypeface(null, Typeface.BOLD)
                    nav_approveJbs?.setTextColor(resources.getColor(R.color.red))
                    nav_approveJbs?.setText("( $qty )")
                }
            })
//        //=====================================================================================================================================================
            // Measurements are completed needs approval for payment
            val m_approval = mainActivityViewModel.getJobApproveMeasureForActivityId(ActivityIdConstants.MEASURE_COMPLETE)
            m_approval.observe(this, androidx.lifecycle.Observer { job_s ->
                qty = job_s.size
                if (qty == 0) {
                    nav_approvMeasure?.setText("")
                } else {
                    nav_approvMeasure?.setGravity(Gravity.CENTER_VERTICAL)
                    nav_approvMeasure?.setTypeface(null, Typeface.BOLD)
                    nav_approvMeasure?.setTextColor(resources.getColor(R.color.red))
                    nav_approvMeasure?.setText("( $qty )")
                }
            })

       }
    }

    override fun onResume() {
        super.onResume()
    if( this.delegate.localNightMode.equals( AppCompatDelegate.MODE_NIGHT_YES))
        HOME.equals(true)
        // initializeCountDrawer()
//        refreshData()
    }

    private fun refreshData() {
        Coroutines.main {
            val contracts = mainActivityViewModel.offlinedata.await()
            contracts.observe(this, Observer { contrcts ->

            })
        }
    }


    companion object {
        private val TAG = MainActivity::class.java.simpleName
        const val HOME2 = "general_switch"


        var switch: Switch? = null
        const val PREFS_NAME = "DarkeModeSwitch"
    }



}
