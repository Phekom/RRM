package za.co.xisystems.itis_rrm.ui.mainview.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_add_project_items.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.MainActivity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment


class AddProjectFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()
    private var navController: NavController? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.title = getString(R.string.new_job)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        navController = this.activity?.let { Navigation.findNavController(it, R.id.nav_host_fragment) }
        return inflater.inflate(R.layout.fragment_add_project_items, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
           createViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(CreateViewModel::class.java) } ?: throw Exception("Invalid Activity")



        last_lin.visibility = View.GONE

        createViewModel.contract_No.observe(viewLifecycleOwner, Observer { contrct ->
            selectedContractTextView.text = contrct
        })
        createViewModel.project_Code.observe(viewLifecycleOwner, Observer { pro_code ->
            selectedProjectTextView.text = pro_code
        })


        //        mid_lin.visibility = View.GONE

//        selectProjectItemdrop.visibility = View.GONE
//        photoLin.visibility = View.GONE


        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {
                R.id.addItemButton -> {
//                    if (selectedProject == null) {
//                        toast("Error: selectedProject is null!")
//                    } else {
                        navController?.navigate(R.id.action_addProjectFragment_to_selectItemFragment)
//                        last_lin.visibility = View.GONE
//                        mid_lin.visibility = View.GONE
//                        selectProjectItemdrop.visibility = View.VISIBLE
//                    }
//                    last_lin.visibility = View.GONE
//                    mid_lin.visibility = View.GONE
//
//                    photoLin.visibility = View.GONE
//                    selectProjectItemdrop.visibility = View.VISIBLE

                }

                R.id.resetButton -> {
                    onResetClicked()
                }

                R.id.infoTextView -> {
                    infoTextView.startAnimation(click)
                }

            }
        }
        addItemButton.setOnClickListener(myClickListener)
        resetButton.setOnClickListener(myClickListener)
        infoTextView.setOnClickListener(myClickListener)



    }

    private fun onResetClicked() {
//        setJob(null)
        resetContractAndProjectSelection()
    }

    fun resetContractAndProjectSelection() {
        navController?.navigate(R.id.action_addProjectFragment_to_nav_create)
//        items.clear()
//        adapter?.setData(items)
//        descriptionEditText.text!!.clear()
////        mid_lin.visibility = View.GONE
////        last_lin.visibility = View.GONE
////        selectProjectItemdrop.visibility = View.GONE
////        photoLin.visibility = View.GONE
//        selectProjectLayout.visibility = View.VISIBLE
//        setLayoutsVisibility()
//        setMenuItems()
    }



}