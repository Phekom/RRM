package za.co.xisystems.itis_rrm.ui.mainview.corrections

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
import za.co.xisystems.itis_rrm.utils.Coroutines


/**
 * Created by Francis Mahlava on 03,October,2019
 */


class CorrectionsFragment : BaseFragment(), KodeinAware {

    override val kodein by kodein()
    private lateinit var correctionsViewModel: CorrectionsViewModel
    private val factory: CorrectionsViewModelFactory by instance()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_correction, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        correctionsViewModel = activity?.run {
            ViewModelProviders.of(this, factory).get(CorrectionsViewModel::class.java)
        } ?: throw Exception("Invalid Activity") as Throwable
        Coroutines.main {
            //            data2_loading.show()
//            val user = homeViewModel.user.await()
//            user.observe(viewLifecycleOwner, Observer { user_ ->
//                username?.setText(user_.userName)
//            })
//
//            val contracts = homeViewModel.offlinedata.await()
//            contracts.observe(viewLifecycleOwner, Observer { contrcts ->
//                group2_loading.visibility = View.GONE
//            })
        }
    }

}



