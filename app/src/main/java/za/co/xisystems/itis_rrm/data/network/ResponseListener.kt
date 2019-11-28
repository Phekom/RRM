package za.co.xisystems.itis_rrm.data.network

// Model/DAL ResponseListener, should not touch on the UI
interface ResponseListener {
    fun onSuccess(num: Int)
    fun onFailure(message: String)
    fun onError(message: String)
}
