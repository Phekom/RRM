package za.co.xisystems.itis_rrm.data.network

// Model/DAL OfflineListener, should not touch on the UI
interface OfflineListener {
    fun onStarted()
    fun onSuccess()
    fun onFailure(message: String?)
}
