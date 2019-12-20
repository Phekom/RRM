package za.co.xisystems.itis_rrm.data._commons

interface ICallBack<T> {
    fun processData(response: T?)
    fun processError(errorMessage: String?)
}