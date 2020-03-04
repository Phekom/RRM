package za.co.xisystems.itis_rrm.ui.mainview.corrections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
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

    override fun onDestroyView() {
        super.onDestroyView()
        if (view != null) {
            val parent = view!!.parent as ViewGroup
            parent.removeAllViews()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_correction, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        correctionsViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CorrectionsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
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



