package za.co.xisystems.itis_rrm.ui.mainview.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.fragment_createjob.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.auth.AuthViewModel
import za.co.xisystems.itis_rrm.ui.auth.AuthViewModelFactory
import za.co.xisystems.itis_rrm.ui.mainview.home.HomeViewModel
import za.co.xisystems.itis_rrm.ui.mainview.home.HomeViewModelFactory
import za.co.xisystems.itis_rrm.utils.Coroutines
//simple_spinner_item

class CreateFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var authViewModel: AuthViewModel
    private val factory: CreateViewModelFactory by instance()
    private val factoryAuth: AuthViewModelFactory by instance()
    private val factoryhome: HomeViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_createjob, container, false)
    }

    override fun onDestroyView() {

        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createViewModel = ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)
        authViewModel = ViewModelProviders.of(this, factoryAuth).get(AuthViewModel::class.java)

        mid_lin.visibility = View.GONE
        last_lin.visibility = View.GONE
        selectProjectdrop.visibility = View.GONE
        photoLin.visibility = View.GONE

        Coroutines.main {

            val contracts = authViewModel.offlinedata.await()
            contracts.observe(viewLifecycleOwner, Observer {
                val contract = arrayOfNulls<String>(it.size)
                for (i in 0 until it.size) {
                    contract[i] = it.get(i).contractNo

                }
                val arrayadapter = ArrayAdapter(
                    context!!.applicationContext,
                    android.R.layout.simple_spinner_dropdown_item,
                    contract
                )
                contractSpinner.adapter = arrayadapter

                val projects = arrayOfNulls<String>(it.size)
                for (i in 0 until it.size) {
                    projects[i] = it.get(i).shortDescr
                }

//                val sub_divisions = arrayOfNulls<String>(it.size)
//                for (i in 0 until it.size) {
//                    sub_divisions[i] = it.get(i).projectCode
//                }
//                Toast.makeText(context?.applicationContext,it.size.toString(),Toast.LENGTH_SHORT).show()

            })



//            val projects = homeViewModel.projectsItems.await()
//            projects.observe(viewLifecycleOwner,Observer {
//                val sub_divisions = arrayOfNulls<String>(it.size)
//                for (i in 0 until it.size) {
////                    sub_divisions[i] = it.get(i).projectCode
//                }


//            })



//
//            contractSpinner.setOnItemSelectedListener(object :
//                AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    adapterView: AdapterView<*>,
//                    view: View,
//                    i: Int,
//                    l: Long
//                ) {
////                        val selectedDivision = it[i]
//                    if (i == 0) {
//                        val arrayadapter = ArrayAdapter(
//                            context!!.applicationContext,
//                            android.R.layout.simple_spinner_dropdown_item,
//                            sub_divisions
//                        )
//                        selectedContractTextView.text = (item[i].toString())
//                        selectedProjectTextView.text = (item[i].toString())
//                        projectSpinner.setAdapter(arrayadapter)
//                    }
//
//                    if (i == 1) {
//                        val arrayadapter = ArrayAdapter(
//                            context!!.applicationContext,
//                            android.R.layout.simple_spinner_dropdown_item,
//                            projects
//                        )
//                        projectSpinner.setAdapter(arrayadapter)
//
//                    }
//
////                        if (i == 2) {
////                            val adapter5: ArrayAdapter<String>
////                            adapter5 = ArrayAdapter<String>(
////                                context?.applicationContext,
////                                android.R.layout.simple_spinner_dropdown_item,
////                                select
////                            )
////                            projectSpinner.setAdapter(adapter5)
////                            projectSpinner.setOnItemSelectedListener(object :
////                                AdapterView.OnItemSelectedListener {
////                                override fun onItemSelected(
////                                    adapterView: AdapterView<*>,
////                                    view: View,
////                                    i: Int,
////                                    l: Long
////                                ) {
////                                    val projects = Projects[i]
////                                    if (i == 0) {
////
////                                    }
////                                    if (i == 1) {
////                                        Toast.makeText(context, "" + projects, Toast.LENGTH_SHORT)
////                                            .show()
////
////                                    }
////                                }
////
////                                override fun onNothingSelected(adapterView: AdapterView<*>) {
////
////                                }
////                            })
////                        }
////
////                        if (i == 3) {
////                            val adapter4: ArrayAdapter<String>
////                            adapter4 = ArrayAdapter<String>(
////                                context,
////                                android.R.layout.simple_spinner_dropdown_item,
////                                Projects1
////                            )
////                            projectSpinner.setAdapter(adapter4)
////
////                        }
////
////                        if (i == 4) {
////                            val adapter4: ArrayAdapter<String>
////                            adapter4 = ArrayAdapter<String>(
////                                context,
////                                android.R.layout.simple_spinner_dropdown_item,
////                                services
////                            )
////                            projectSpinner.setAdapter(adapter4)
////
////                        }
//
////                        Toast.makeText(context, "" + selectedDivision, Toast.LENGTH_SHORT).show()
//
//                }
//
//                override fun onNothingSelected(adapterView: AdapterView<*>) {
//
//                }
//            })

            val myClickListener = object : View.OnClickListener {
                override fun onClick(view: View?) {
                    when (view?.id) {
                        R.id.selectContractProjectContinueButton -> {
                            if (descriptionEditText.text!!.isEmpty()) {
                                Toast.makeText(context, "Please Enter Description", Toast.LENGTH_SHORT)
                                    .show()
                                return
                            }
                            selectProjectLayout.visibility = View.GONE
                            mid_lin.visibility = View.VISIBLE
                            infoTextView.visibility = View.GONE



                        }

                        R.id.infoTextView -> {
                            selectProjectLayout.visibility = View.GONE
                            mid_lin.visibility = View.GONE
                            selectProjectdrop.visibility = View.GONE
                            photoLin.visibility = View.VISIBLE
                            last_lin.visibility = View.GONE
                        }

                        R.id.addItemButton -> {
                            selectProjectLayout.visibility = View.GONE
                            mid_lin.visibility = View.GONE
                            photoLin.visibility = View.GONE
                            selectProjectdrop.visibility = View.VISIBLE
                            last_lin.visibility = View.GONE
                        }

                        R.id.updateButton -> {
                            infoTextView.visibility = View.VISIBLE
                            selectProjectdrop.visibility = View.GONE
                            photoLin.visibility = View.GONE
                            mid_lin.visibility = View.VISIBLE
                            last_lin.visibility = View.VISIBLE
                        }

                    }
                }

            }


            selectContractProjectContinueButton.setOnClickListener(myClickListener)
//        selectContractProjectContinueButton.setOnClickListener(myClickListener)
            infoTextView.setOnClickListener(myClickListener)
            addItemButton.setOnClickListener(myClickListener)
            updateButton.setOnClickListener(myClickListener)



            sectionItemSpinner.setOnTouchListener { view, motionEvent ->
                infoTextView.visibility = View.VISIBLE
                selectProjectdrop.visibility = View.GONE
                mid_lin.visibility = View.VISIBLE
                last_lin.visibility = View.VISIBLE
                false
            }





//            val Projects1 = arrayOf("Stara", "Kenani", "Ultra", "Rweba")
//
//            val Projects = arrayOf(
//                "N.001-005-2012/2 DEMO",
//                "S-PROJ 51 - VIBRANT CONST",
//                "S-PROJ34-ALSU",
//                "S-PROJ 32 - ATH",
//                "S-PROJ 33- EGON CIVILS",
//                "S-PROJ 41 - SIMANDIE"
//            )
//
//            val sub_divisions = arrayOf(
//                "N.001-035-2012/1 DEMO",
//                "S-PROJ 41 - SIMANDIE",
//                "S-PROJ 33- EGON CIVILS",
//                "S-PROJ 51 - VIBRANT CONST",
//                "Yes"
//            )
//
//            val select = arrayOf("---select project --")
//            val select1 = arrayOf("---select service --")
//
//            val Projects2 = arrayOf(
//                "--Select Upozella--",
//                "Lalbag",
//                "Islambag",
//                "Chawkbazar",
//                "Shahbag",
//                "New Market"
//            )

//            val services = arrayOf(
//                "--Select Service--",
//                "service1",
//                "service2",
//                "service3",
//                "service4",
//                "service5",
//                "service6"
//            )


        }





    }


}

//            val contracts =
//                arrayOf(
////                "---Select Contracts---",
//                    "N.001-035-2012/1 DEMO",
//                    "N.001-005-2012/2 DEMO",
//                    "R.061-058-2013/1 DEMO",
//                    "N.001-035-2012/1 ",
//                    "N.001-005-2012/2 ",
//                    "R.061-058-2013/1 "
//                )
