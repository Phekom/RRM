/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 16:38
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.network

import com.google.gson.JsonObject
import java.util.concurrent.TimeUnit
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
    ): Response<AuthResponse>

    @FormUrlEncoded
    @POST("VersionCheck")
    suspend fun versionCheck(
        @Field("VersionNum") versionNum: String
    ): Response<VersionCheckResponse>

    @FormUrlEncoded
    @POST("HealthCheck")
    suspend fun healthCheck(
        @Field("UserLogon") userLogon: String
    ): Response<HealthCheckResponse>

    @FormUrlEncoded
    @POST("UpdateEstQty")
    suspend fun updateEstimateQty(
        @Field("EstimateId") estimateId: String?,
        @Field("Quantity") quantity: Double?,
        @Field("TotalAmount") totalAmount: Double?
    ): Response<QuantityUpdateResponse>

    @FormUrlEncoded
    @POST("UpdateMeasureQty")
    suspend fun upDateMeasureQty(
        @Field("MeasurementId") newMeasureid: String?,
        @Field("Quantity") quantity: Double?
    ): Response<QuantityUpdateResponse>

    @FormUrlEncoded
    @POST("RrmActivitySectionsRefresh")
    suspend fun activitySectionsRefresh(
        @Field("UserId") userId: String
    ): Response<ActivitySectionsResponse>

    @FormUrlEncoded
    @POST("GetAllContractsByUserId")
    suspend fun getAllContractsByUserId(
        @Field("UserId") userId: String
    ): Response<ContractsResponse>

    @FormUrlEncoded
    @POST("WorkflowsRefresh")
    suspend fun workflowsRefresh(
        @Field("UserId") userId: String
    ): Response<WorkflowResponse>

    @FormUrlEncoded
    @POST("MobileLookupsRefresh")
    suspend fun lookupsRefresh(
        @Field("UserId") userId: String
    ): Response<LookupResponse>

    @FormUrlEncoded
    @POST("GetUserTaskList")
    suspend fun getUserTaskList(
        @Field("UserId") userId: String
    ): Response<ToDoListGroupsResponse>

    @FormUrlEncoded
    @POST("GetRRMJob")
    suspend fun getJobsForApproval(
        @Field("JobId") jobId: String
    ): Response<JobResponse>

    @FormUrlEncoded
    @POST("GetRrmJobPhotoEstimate")
    suspend fun getPhotoEstimate(
        @Field("FileName") fileName: String
    ): Response<PhotoEstimateResponse>

    @FormUrlEncoded
    @POST("GetRrmJobPhoto")
    suspend fun getPhotoMeasure(
        @Field("FileName") fileName: String
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
    @POST("GetRouteSectionPoint")
    suspend fun getRouteSectionPoint(
        @Field("Distance") distance: Double,
        @Field("MustBeInBuffer") buffer: Double,
        @Field("Latitude") latitude: Double,
        @Field("Longitude") longitude: Double,
        @Field("UserId") userId: String
    ): Response<RouteSectionPointResponse>

    @POST("UpdateApprovalInfo")
    suspend fun updateApprovalInfo(
        @Body updateApprovalRequest: JsonObject
    ): Response<WorkflowUpdateResponse>

    @POST("UpdateWorkStartInfo")
    suspend fun updateWorkStartInfo(
        @Body updateWorkStartRequest: JsonObject
    ): Response<WorkflowUpdateResponse>

    @POST("UpdateWorkEndInfo")
    suspend fun updateWorkEndInfo(
        @Body updateWorkEndRequest: JsonObject
    ): Response<WorkflowUpdateResponse>

    @POST("UpdateWorkStateInfo")
    suspend fun updateWorkStateInfo(
        @Body updateWorkStateRequest: JsonObject
    ): Response<WorkflowUpdateResponse>

    @POST("UpdateMeasureCreatedInfo")
    suspend fun updateMeasureCreatedInfo(
        @Body updateMeasureCreatedRequest: JsonObject
    ): Response<WorkflowUpdateResponse>

    @POST("UpdateMeasureApprovalInfo")
    suspend fun updateMeasureApprovalInfo(
        @Body updateMeasureApprovalRequest: JsonObject
    ): Response<WorkflowUpdateResponse>

    @POST("SaveRrmJob")
    suspend fun sendJobsForApproval(
        @Body job: JsonObject
    ): Response<JobResponse>

    @POST("UploadWorksItem")
    suspend fun uploadWorksItem(
        @Body job: JsonObject
    ): Response<UploadWorksItemResponse>

    companion object {
        operator fun invoke(
            networkConnectionInterceptor: NetworkConnectionInterceptor
        ): BaseConnectionApi {
            val okkHttpclient = OkHttpClient
                .Builder().apply {
                    callTimeout(10, TimeUnit.MINUTES)
                    readTimeout(5, TimeUnit.MINUTES)
                    writeTimeout(5, TimeUnit.MINUTES)
                    connectTimeout(5, TimeUnit.MINUTES)
                    protocols(listOf(Protocol.HTTP_1_1))
                        .pingInterval(10000, TimeUnit.MILLISECONDS)

                    addInterceptor(networkConnectionInterceptor)
                    addInterceptor { chain ->
                        var request = chain.request()
                        request = request.newBuilder().build()
                        val response = chain.proceed(request)
                        response
                    }
                }

            /**
             * Add the http logging interceptor.
             * Debug build only.
             */
            if (BuildConfig.DEBUG) {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                okkHttpclient.addInterceptor(interceptor)
            }

            return Retrofit.Builder()
                //.addCallAdapterFactory(NetworkResponseAdapterFactory())
                .client(okkHttpclient.build())
                .baseUrl(BuildConfig.API_HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BaseConnectionApi::class.java)
        }


    }
}
