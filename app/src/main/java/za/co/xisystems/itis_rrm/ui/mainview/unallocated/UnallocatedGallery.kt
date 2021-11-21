package za.co.xisystems.itis_rrm.ui.mainview.unallocated

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.xisystems.itis_rrm.R

class UnallocatedGallery: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_unallocated_gallery, container, false)
    }
}