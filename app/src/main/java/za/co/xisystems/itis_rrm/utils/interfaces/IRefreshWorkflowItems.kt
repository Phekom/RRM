package za.co.xisystems.itis_rrm.utils.interfaces

/**
 * Created by Francis Mahlava on 2019/11/30.
 */
interface IRefreshWorkflowItems {
    fun onRequestPermissions()
    fun onNoConnection()
    fun onError()
    fun onSuccess()
}
