package za.co.xisystems.itis_rrm.data.network

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import za.co.xisystems.itis_rrm.BuildConfig
import za.co.xisystems.itis_rrm.data.network.responses.*
import java.util.concurrent.TimeUnit

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
const val BASE_URL = BuildConfig.API_HOST

interface BaseConnectionApi {

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("Register")
    suspend fun userRegister(
        @Field("device") device: String,
        @Field("imei") imei: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("username") userlogon: String,
        @Field("userpassword") userpwd: String
    ): Response<AuthResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("HealthCheck")
    suspend fun healthCheck(
        @Field("UserLogon") UserLogon: String
    ): Response<HealthCheckResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("RrmActivitySectionsRefresh")
    suspend fun activitySectionsRefresh(
        @Field("UserId") UserId: String
    ): Response<ActivitySectionsResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("ContractInfoRefresh")
    suspend fun refreshContractInfo(
        @Field("UserId") UserId: String
    ): Response<ContractsResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("WorkflowsRefresh")
    suspend fun workflowsRefresh(
        @Field("UserId") UserId: String
    ): Response<WorkflowResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("MobileLookupsRefresh")
    suspend fun lookupsRefresh(
        @Field("UserId") UserId: String
    ): Response<LookupResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetUserTaskList")
    suspend fun getUserTaskList(
        @Field("UserId") UserId: String
    ): Response<ToDoListGroupsResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetRRMJob")
    suspend fun getJobsForApproval(
        @Field("JobId") JobId: String
    ): Response<JobResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetRrmJobPhotoEstimate")
    suspend fun getPhotoEstimate(
        @Field("FileName") FileName: String
    ): Response<PhotoEstimateResponse>

    @FormUrlEncoded
//    @Headers("Content-Type: application/json")
    @POST("GetRrmJobPhoto")
    suspend fun getPhotoMeasure(
        @Field("FileName") FileName: String
    ): Response<PhotoMeasureResponse>

    @FormUrlEncoded
    @POST("WorkflowMoveV2")
    suspend fun getWorkflowMove(
        @Field("UserId") userId: String,
        @Field("TrackRouteId") trackRouteId: String,
        @Field("Description") description: String?,
        @Field("Direction") direction: Int
    ): Response<WorkflowMoveResponse>

    @POST("SaveMeasurementItems")
    suspend fun saveMeasurementItems(
        @Body measurementItems: JsonObject
    ): Response<SaveMeasurementResponse>

    @POST("UploadRrmImage")
    suspend fun uploadRrmImage(
        @Body rrmImage: JsonObject
    ): Response<UploadImageResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetRouteSectionPoint")
    suspend fun getRouteSectionPoint(

        @Field("Distance") distance : Double,
        @Field("MustBeInBuffer") buffer: Int,
        @Field("Latitude") latitude: Double,
        @Field("Longitude") longitude: Double,
        @Field("UserId") UserId: String
    ): Response<RouteSectionPointResponse>



    @POST("SaveRrmJob")
    suspend fun sendJobsForApproval(
        @Body job: JsonObject
    ): Response<JobResponse>


    @POST("UploadWorksItem")
    suspend fun uploadWorksItem(
        @Body job: JsonObject
    ): Response<UploadWorksItemResponse>













//        @Field("UserId") userId: String,
//        @Field("JobId") jobId: String,
//        @Field("JiNo") jimNo: String?,
//        @Field("ContractId") contractVoId: String?,
//        @Field("MeasurementItems[]") measurementItems: ArrayList<JobItemMeasureDTOTemp>?
//    @Headers("Content-Type: application/json")
//    @FormUrlEncoded
//    @POST("SaveMeasurementItems")
//    suspend fun saveMeasurementItems(
//        @Field("UserId") userId: String,
//        @Field("JobId") jobId: String,
//        @Field("JiNo") jimNo: String?,
//        @Field("ContractId") contractVoId: String?,
//        @Field("MeasurementItems") measurementItems: ArrayList<JobItemMeasureDTOTemp>
//    ):  Response<SaveMeasurementResponse>
//    @FormUrlEncoded
    //    @FormUrlEncoded
//    @POST("UploadRrmImage")
//    suspend fun uploadRrmImage(
//        @Field("Filename") Filename: String,
//        @Field("ImageFileExtension") ext: String,
//        @Field("ImageByteArray") photo: ByteArray
//    ): Response<UploadImageResponse>

//    @POST("UploadRrmImage")
//    suspend fun sendJobsForApproval(
//
//    ): Response<JobResponse>

//    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
//    @POST("UserRolesRefresh")
//    suspend fun userRoles(
//        @Field("UserId") UserId : String
//    ) : Response<UserRoleResponse>



















    companion object {
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): BaseConnectionApi {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val okkHttpclient = OkHttpClient
                .Builder().apply {
                readTimeout(5, TimeUnit.MINUTES)
                writeTimeout(5, TimeUnit.MINUTES)
                connectTimeout(5, TimeUnit.MINUTES)
                    protocols(listOf(Protocol.HTTP_1_1))
//                .pingInterval(100, TimeUnit.MILLISECONDS)

                addInterceptor(networkConnectionInterceptor)
                addInterceptor { chain ->
                    var request = chain.request()
                    request = request.newBuilder().build()
                    val response = chain.proceed(request)
                    response
                }

            }

            if (BuildConfig.DEBUG)
                okkHttpclient.addInterceptor(interceptor)

            return Retrofit.Builder()
                .client(okkHttpclient.build())
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BaseConnectionApi::class.java)

        }
    }


}














































































//            return Retrofit.Builder()
//                .client(okkHttpclient)
//
//                .baseUrl("https://itisqa.nra.co.za/ITISServicesMobile/api/RRM/")
//                .addConverterFactory(JacksonConverterFactory.create())
////                .addConverterFactory(GsonConverterFactory.create())
////                .addCallAdapterFactory(CoroutineCallAdapterFactory())
//                .build()
//                .create(BaseConnectionApi::class.java)