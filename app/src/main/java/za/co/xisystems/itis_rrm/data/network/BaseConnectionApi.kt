package za.co.xisystems.itis_rrm.data.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import za.co.xisystems.itis_rrm.data.network.responses.*

/**
 * Created by Francis Mahlava on 2019/10/23.
 */
interface BaseConnectionApi {

    @FormUrlEncoded
    @POST("Register")
    suspend fun userRegister(
        @Field("device") device: String,
        @Field("imei") imei: String,
        @Field("phoneNumber") phoneNumber: String,
        @Field("username") userlogon: String,
        @Field("userpassword") userpwd: String
    ) : Response<AuthResponse>

    @FormUrlEncoded
    @POST("HealthCheck")
    suspend fun HealthCheck(
        @Field("UserLogon") UserLogon : String
    ) : Response<HealthCheckResponse>

    @FormUrlEncoded
    @POST("RrmActivitySectionsRefresh")
    suspend fun ActivitySectionsRefresh(
        @Field("UserId") UserId : String
    ) : Response<ActivitySectionsResponse>


    @FormUrlEncoded
    @POST("ContractInfoRefresh")
    suspend fun refreshContractInfo(
        @Field("UserId") UserId : String
    ) : Response<ContractsResponse>


    @FormUrlEncoded
    @POST("WorkflowsRefresh")
    suspend fun workflowsRefresh(
        @Field("UserId") UserId : String
    ) : Response<WorkflowResponse>


    @FormUrlEncoded
    @POST("MobileLookupsRefresh")
    suspend fun lookupsRefresh(
        @Field("UserId") UserId : String
    ) : Response<LookupResponse>

    @FormUrlEncoded
    @POST("GetUserTaskList")
    suspend fun getUserTaskList(
        @Field("UserId") UserId : String
    ) : Response<ToDoListGroupsResponse>


//    @FormUrlEncoded
//    @POST("GetProjectItems")
//    suspend fun getProjectItems(
//        @Field("ProjectId") ProjectId : String
//    ) : Response<ItemsResponse>
//
//
//    @FormUrlEncoded
//    @POST("ProjectItemsRefresh")
//    suspend fun projectItemsRefresh(
//        @Field("ProjectId") ProjectId : String
//    ) : Response<ItemsResponse>
//
//    @FormUrlEncoded
//    @POST("ProjectVosRefresh")
//    suspend fun projectVosRefresh(
//        @Field("ProjectId") ProjectId : String
//    ) : Response<VoItemsResponse>


//    @FormUrlEncoded
//    @POST("UserRolesRefresh")
//    suspend fun userRoles(
//        @Field("UserId") UserId : String
//    ) : Response<UserRoleResponse>










    companion object{
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ) : BaseConnectionApi{

            val okkHttpclient = OkHttpClient.Builder()
//                .connectTimeout(2, TimeUnit.MINUTES)
//                .writeTimeout(2, TimeUnit.MINUTES) // write timeout
//                .readTimeout(2, TimeUnit.MINUTES) // read timeout
                .addInterceptor(networkConnectionInterceptor)
                .build()

            return Retrofit.Builder()
                .client(okkHttpclient)
                .baseUrl("https://itisqa.nra.co.za/ITISServicesMobile/api/RRM/")
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BaseConnectionApi::class.java)
        }
    }



}