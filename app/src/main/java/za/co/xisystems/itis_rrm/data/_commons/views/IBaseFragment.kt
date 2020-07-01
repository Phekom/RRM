package za.co.xisystems.itis_rrm.data._commons.views

import androidx.fragment.app.Fragment

interface IBaseFragment<T : Fragment> {
    val activity: T
    fun showProgressDialog()
    fun dismissProgressDialog()
}
