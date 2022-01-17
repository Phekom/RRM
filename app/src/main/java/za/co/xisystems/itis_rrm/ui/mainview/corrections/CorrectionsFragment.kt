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

/**
 * Created by Francis Mahlava on 03,October,2019
 */

class CorrectionsFragment : BaseFragment(), DIAware {
    //
    override val di by closestDI()
    private lateinit var correctionsViewModel: CorrectionsViewModel
    private val factory: CorrectionsViewModelFactory by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        correctionsViewModel =
            ViewModelProvider(this.requireActivity(), factory).get(CorrectionsViewModel::class.java)
    }

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
}
