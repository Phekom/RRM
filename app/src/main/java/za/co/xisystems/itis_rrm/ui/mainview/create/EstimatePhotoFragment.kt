package za.co.xisystems.itis_rrm.ui.mainview.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.mainview._fragments.BaseFragment



class EstimatePhotoFragment : BaseFragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var createViewModel: CreateViewModel
    private val factory: CreateViewModelFactory by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_photo_estimate, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        createViewModel = ViewModelProviders.of(this, factory).get(CreateViewModel::class.java)




        val myClickListener = View.OnClickListener { view ->
            when (view?.id) {

//                R.id.updateButton -> {
//                    infoTextView.visibility = View.VISIBLE
//                    selectProjectdrop.visibility = View.GONE
//                    photoLin.visibility = View.GONE
//                    mid_lin.visibility = View.VISIBLE
//                    last_lin.visibility = View.VISIBLE
//                }




            }
        }



    }

//        selectContractProjectContinueButton.setOnClickListener(myClickListener)
//

//        updateButton.setOnClickListener(myClickListener)





}