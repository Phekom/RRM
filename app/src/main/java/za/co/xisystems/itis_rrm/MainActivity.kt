package za.co.xisystems.itis_rrm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import za.co.xisystems.itis_rrm.data.localDB.AppDatabase
import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO
import za.co.xisystems.itis_rrm.ui.auth.LoginActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.SettingsActivity
import za.co.xisystems.itis_rrm.utils.hideKeyboard


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var navController: NavController? = null
    var toggle : ActionBarDrawerToggle? = null
    private lateinit var navigationView: NavigationView
    private val Db: AppDatabase? = null

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
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            val intent = Intent()
//            val packageName = packageName
//            val pm =
//                getSystemService(Context.POWER_SERVICE) as PowerManager
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//                intent.data = Uri.parse("package:$packageName")
//                startActivity(intent)
//            }
//        }
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings ->{
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.logout ->{
                startActivity(Intent(this, LoginActivity::class.java))
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
    fun getUserRoles(roleList:  LiveData<List<UserRoleDTO>>) {

        val menuNav = navigationView.getMenu()
        var nav_item: MenuItem
        //[roles]

//        for (role in roleList) {
//            val IDss = role.roleIdentifier
//
//            if (IDss.equals(PROJECT_USER_ROLE_IDENTIFIER, ignoreCase = true)) {
////                initializeCountDrawer()
//                nav_item = menuNav.findItem(R.id.nav_create)
//                nav_item.isEnabled = true
//
//                nav_item = menuNav.findItem(R.id.nav_unSubmitted)
//                nav_item.isEnabled = true
//
//                nav_item = menuNav.findItem(R.id.nav_correction)
//                nav_item.isEnabled = false
//
//                nav_item = menuNav.findItem(R.id.nav_work)
//                nav_item.isEnabled = false
//
//                nav_item = menuNav.findItem(R.id.nav_approveJbs)
//                nav_item.isEnabled = false
//
//                nav_item = menuNav.findItem(R.id.nav_estMeasure)
//                nav_item.isEnabled = false
//
//                nav_item = menuNav.findItem(R.id.nav_approvMeasure)
//                nav_item.isEnabled = false
//
//
//            }
//
//            if (IDss.equals(PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true)) {
////                initializeCountDrawer()
//                val nav_item1 = menuNav.findItem(R.id.nav_create)
//                nav_item1.setEnabled(false)
//
//                val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
//                nav_item2.setEnabled(false)
//
//                val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                nav_item3.setEnabled(false)
//
//                val nav_item4 = menuNav.findItem(R.id.nav_work)
//                nav_item4.setEnabled(true)
//
//                val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
//                nav_item5.setEnabled(false)
//
//                val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
//                nav_item6.setEnabled(false)
//
//                val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
//                nav_item7.setEnabled(false)
//
//
//            }
//
//            if (IDss.equals(PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true)) {
////                initializeCountDrawer()
//                val nav_item1 = menuNav.findItem(R.id.nav_create)
//                nav_item1.setEnabled(true)
//
//                val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
//                nav_item2.setEnabled(true)
//
//                val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                nav_item3.setEnabled(false)
//
//                val nav_item4 = menuNav.findItem(R.id.nav_work)
//                nav_item4.setEnabled(true)
//
//                val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
//                nav_item5.setEnabled(false)
//
//                val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
//                nav_item6.setEnabled(true)
//
//                val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
//                nav_item7.setEnabled(false)
//
//
//            }
//
//            if (IDss.equals(PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true)) {
////                initializeCountDrawer()
//                val nav_item1 = menuNav.findItem(R.id.nav_create)
//                nav_item1.setEnabled(true)
//
//                val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
//                nav_item2.setEnabled(true)
//
//                val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                nav_item3.setEnabled(false)
//
//                val nav_item4 = menuNav.findItem(R.id.nav_work)
//                nav_item4.setEnabled(false)
//
//                val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
//                nav_item5.setEnabled(false)
//
//                val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
//                nav_item6.setEnabled(true)
//
//                val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
//                nav_item7.setEnabled(false)
//
//
//            }
//
//            if (IDss.equals(PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true)) {
////                initializeCountDrawer()
//                val nav_item1 = menuNav.findItem(R.id.nav_create)
//                nav_item1.setEnabled(false)
//
//                val nav_item2 = menuNav.findItem(R.id.nav_unSubmitted)
//                nav_item2.setEnabled(false)
//
//                val nav_item3 = menuNav.findItem(R.id.nav_correction)
//                nav_item3.setEnabled(false)
//
//                val nav_item4 = menuNav.findItem(R.id.nav_work)
//                nav_item4.setEnabled(false)
//
//                val nav_item5 = menuNav.findItem(R.id.nav_approveJbs)
//                nav_item5.setEnabled(true)
//
//                val nav_item6 = menuNav.findItem(R.id.nav_estMeasure)
//                nav_item6.setEnabled(false)
//
//                val nav_item7 = menuNav.findItem(R.id.nav_approvMeasure)
//                nav_item7.setEnabled(true)
//
//
//            }
//        }

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






































}
