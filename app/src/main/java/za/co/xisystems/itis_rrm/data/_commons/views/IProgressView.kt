package za.co.xisystems.itis_rrm.data._commons.views

interface IProgressView {
    fun showProgressDialog(vararg messages: String?)
    fun dismissProgressDialog()
    fun toast(resid: Int)
    fun toast(resid: String?)
}
