package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data._commons.views.ToastUtils
import za.co.xisystems.itis_rrm.databinding.ActivityJobCreationBinding
import za.co.xisystems.itis_rrm.ui.base.BaseActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivity
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivityViewModel
import za.co.xisystems.itis_rrm.ui.mainview.activities.main.MainActivityViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines

class JobCreationActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mainActivityViewModel: JobCreationViewModel
    private val factory: JobCreationViewModelFactory by instance()

    private lateinit var binding: ActivityJobCreationBinding
    var navController: NavController? = null
    internal var selectedFragmnetID: String? = null
    companion object {
        val TAG: String = JobCreationActivity::class.java.simpleName
        private const val PERMISSION_REQUEST = 10
        const val FRAG_ID = "FRAG_ID"
        const val FRAGMNT = "FRAGMNT"
    }
    lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJobCreationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.mainActivityViewModel = ViewModelProvider(this, factory)[JobCreationViewModel::class.java]
        navigationView  = binding.navviewbottom
        navController = findNavController(R.id.nav_host_fragment_activity_job_creation)
        NavigationUI.setupWithNavController(navigationView, navController!!)


        if (intent.extras != null && intent.extras!!.containsKey(FRAGMNT)) {
//          selectedLocalID = intent.getStringExtra(LOCAL_ID)
            selectedFragmnetID = intent.getStringExtra(FRAG_ID)
            val openFrag: Boolean = intent.extras!!.getBoolean(FRAGMNT)
            if (openFrag) {

                when (selectedFragmnetID) {
                    "navigation_un_submitted" -> {
                        navigationView.visibility =  View.GONE
                        navController?.navigate(R.id.navigation_un_submitted)
                    }

                    "navigation_create" -> {
                        navController?.navigate(R.id.navigation_create)
                    }

                    "navigation_work" -> {
                        navController?.navigate(R.id.navigation_work)
                    }

                    "navigation_measure_est" -> {
                        navController?.navigate(R.id.navigation_measure_est)
                    }

                }
            }
        } else{

        }



        getUserRoles()
        setBottomMenu()

    }

    private fun getUserRoles() {
        Coroutines.main {
            // Initially disable all menu options
            val menuNav = navigationView.menu

            val navCreate = menuNav.findItem(R.id.navigation_create)
            navCreate.isEnabled = false

            val navWork = menuNav.findItem(R.id.navigation_work)
            navWork.isEnabled = false


            val navEstMeasures = menuNav.findItem(R.id.navigation_measure_est)
            navEstMeasures.isEnabled = false

            val userRoles = mainActivityViewModel.getRoles()
            userRoles.observe(this, { roleList ->

                for (role in roleList) {
                    val roleID = role.roleDescription
                    when {
                        roleID.equals(MainActivity.PROJECT_USER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navCreate.isVisible = true
                            navWork.isEnabled = false
                            navWork.isVisible = false
                            navEstMeasures.isEnabled = false
                            navEstMeasures.isVisible = false
                        }

                        roleID.equals(MainActivity.PROJECT_SUB_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = false
                            navCreate.isVisible = false
                            navWork.isEnabled = true
                            navWork.isVisible = true
                            navEstMeasures.isEnabled = false
                            navEstMeasures.isVisible = false

                        }

                        roleID.equals(MainActivity.PROJECT_CONTRACTOR_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navCreate.isVisible = true
                            navWork.isEnabled = true
                            navWork.isVisible = true
                            navEstMeasures.isEnabled = true
                            navEstMeasures.isVisible = true

                        }

                        roleID.equals(MainActivity.PROJECT_SITE_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navCreate.isVisible = true
                            navWork.isEnabled = false
                            navWork.isVisible = false
                            navEstMeasures.isEnabled = true
                            navEstMeasures.isVisible = true

                        }

                        roleID.equals(MainActivity.PROJECT_ENGINEER_ROLE_IDENTIFIER, ignoreCase = true) -> {
                            navCreate.isEnabled = true
                            navCreate.isVisible = true
                            navWork.isEnabled = false
                            navWork.isVisible = false
                            navEstMeasures.isEnabled = true
                            navEstMeasures.isVisible = true

                        }
                    }
                }
            })
        }
    }

    private fun setBottomMenu() {
        binding.navviewbottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    val myIntent = Intent(this.applicationContext, MainActivity::class.java)
                    myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivityForResult(myIntent, 0)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_create -> {
                    navController?.navigate(R.id.navigation_create)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_work -> {
                    navController?.navigate(R.id.navigation_work)
                    return@setOnItemSelectedListener true
                }
                R.id.navigation_measure_est -> {
                    navController?.navigate(R.id.navigation_measure_est)
                    return@setOnItemSelectedListener true
                }
            }

            true
        }
    }

    override fun startLongRunningTask() {

    }

    override fun endLongRunningTask() {

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_un_submitted -> {
                navController?.navigate(R.id.navigation_un_submitted)

            }
            R.id.navigation_un_submitted -> {
                navController?.navigate(R.id.navigation_un_submitted)

            }
            R.id.navigation_work -> {
                navController?.navigate(R.id.captureWorkFragment)

            }
            R.id.navigation_measure_est -> {
                navController?.navigate(R.id.nav_estMeasure)

            }
        }
        return true
    }


}