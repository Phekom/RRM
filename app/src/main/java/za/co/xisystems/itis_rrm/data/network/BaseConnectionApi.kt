package za.co.xisystems.itis_rrm.data.network

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import za.co.xisystems.itis_rrm.data.network.responses.*
import java.util.concurrent.TimeUnit

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
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
    ) : Response<AuthResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("HealthCheck")
    suspend fun healthCheck(
        @Field("UserLogon") UserLogon : String
    ) : Response<HealthCheckResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("RrmActivitySectionsRefresh")
    suspend fun activitySectionsRefresh(
        @Field("UserId") UserId : String
    ) : Response<ActivitySectionsResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("ContractInfoRefresh")
    suspend fun refreshContractInfo(
        @Field("UserId") UserId : String
    ) : Response<ContractsResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("WorkflowsRefresh")
    suspend fun workflowsRefresh(
        @Field("UserId") UserId : String
    ) : Response<WorkflowResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("MobileLookupsRefresh")
    suspend fun lookupsRefresh(
        @Field("UserId") UserId : String
    ) : Response<LookupResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetUserTaskList")
    suspend fun getUserTaskList(
        @Field("UserId") UserId : String
    ) : Response<ToDoListGroupsResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetRRMJob")
    suspend fun getJobsForApproval(
        @Field("JobId") JobId : String
    ) : Response<JobResponse>

    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
    @POST("GetRrmJobPhotoEstimate")
    suspend fun getPhotoEstimate(
        @Field("FileName") FileName : String
    ) : Response<PhotoEstimateResponse>

    @FormUrlEncoded
//    @Headers("Content-Type: application/json")
    @POST("GetRrmJobPhoto")
    suspend fun getPhotoMeasure(
        @Field("FileName") FileName : String
    ) : Response<PhotoMeasureResponse>

//    @FormUrlEncoded
//    @Headers("Content-Type : application/json")
//    @POST("UserRolesRefresh")
//    suspend fun userRoles(
//        @Field("UserId") UserId : String
//    ) : Response<UserRoleResponse>










    companion object{
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ) : BaseConnectionApi{

            val  okkHttpclient = OkHttpClient.Builder()
                .addInterceptor(networkConnectionInterceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)

                .baseUrl("https://itisqa.nra.co.za/ITISServicesMobile/api/RRM/")
                .addConverterFactory(GsonConverterFactory.create())
//                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .build()
                .create(BaseConnectionApi::class.java)
        }
    }



}