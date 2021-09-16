@file:Suppress("SpellCheckingInspection")

package za.co.xisystems.itis_rrm.ui.mainview.corrections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.base.BaseFragment
import za.co.xisystems.itis_rrm.utils.Coroutines

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class CorrectionsFragment : BaseFragment(), DIAware {
    //
    override val di by closestDI()
    private lateinit var correctionsViewModel: CorrectionsViewModel
    private val factory: CorrectionsViewModelFactory by instance()

    override fun onDestroyView() {
        super.onDestroyView()
        if (view != null) {
            val parent = requireView().parent as ViewGroup
            parent.removeAllViews()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_correction, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // no options menu
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        correctionsViewModel = activity?.run {
            ViewModelProvider(this, factory).get(CorrectionsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
        Coroutines.main {
        }
    }
}
