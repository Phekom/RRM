package za.co.xisystems.itis_rrm.data.network

// Model/DAL OfflineListener, should not touch on the UI
interface OfflineListener {
    fun onStarted()
    fun onSuccess()

    //    fun onSuccess(num: Int)
    fun onFailure(message: String?)
//    fun onError(message: String)
}
