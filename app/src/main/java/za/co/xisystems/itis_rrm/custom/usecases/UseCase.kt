// package za.co.xisystems.itis_rrm.custom.usecases
//
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.Transformations
// import androidx.lifecycle.liveData
// import androidx.lifecycle.map
// import com.airbnb.lottie.L
// import kotlinx.coroutines.CoroutineDispatcher
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.launch
// import kotlinx.coroutines.withContext
// import retrofit2.HttpException
// import retrofit2.Response
// import za.co.xisystems.itis_rrm.custom.errors.NoResponseException
// import za.co.xisystems.itis_rrm.custom.errors.XIErrorHandler
// import za.co.xisystems.itis_rrm.custom.results.XIError
// import za.co.xisystems.itis_rrm.custom.results.XIResult
// import za.co.xisystems.itis_rrm.custom.results.XISuccess
//
//
// abstract class BaseUseCase(private val externalScope: CoroutineScope, private val externalDispatcher: CoroutineDispatcher) {
//
//
//    init {
//        scope = externalScope
//        dispatcher = externalDispatcher
//    }
//
//    protected suspend fun <T: Any, L: Any> execute(
//        roomQuery: () -> LiveData<List<T>>,
//        networkRequest: suspend () -> LiveData<List<T>>,
//        roomSave: suspend (List<T>) -> Unit) = liveData(externalDispatcher) {
//            try {
//                emit(XIResult.progress(true))
//                val queryResults = withContext(externalDispatcher) {
//                    return@withContext (roomQuery()).value
//                }
//                when(queryResult) {
//                   is XISuccess<T> -> {
//                       queryResults?.map { it -> XIResult.success(it) }
//                   }
//                }
//                emit(XIResult.success(queryResults))
//                when (queryResults :)
//                queryResults.data.let { results ->
//                    val workItems: LiveData<L> = transformer(results)
//                }
//            } catch (t: Throwable) {
//
//            }
//
//
//
//        }
//    }
//
//    suspend fun <T: Any> getRemoteData(call: suspend () -> Response<T>): XIResult<T> {
//
//        return try {
//            val response = call()
//            if (response.isSuccessful) {
//                val body = response.body()
//                if (body != null) {
//                    XIResult.success(data = body.toString())
//                } else {
//                    generateError(NoResponseException("The service returned an empty response."), "The service returned an empty response.")
//                }
//            }
//            val httpMessage = "${response.code()} ${response.message()}"
//            generateError(HttpException(response), httpMessage)
//        } as za.co.xisystems.itis_rrm.custom.results.XIResult<T> catch (throwable: Throwable) {
//            generateError(throwable, throwable.message ?: XIErrorHandler.UNKNOWN_ERROR)
//        }
//    }
//
//    protected fun <T> getLocalData(call: () -> LiveData<T>): XIResult<Any> {
//    }
//
//    private fun generateError(throwable: Throwable, errorMessage: String): XIResult<Any> {
//        return XIResult.error(throwable, "Network call has failed for a following reason: $errorMessage")
//    }
// }
