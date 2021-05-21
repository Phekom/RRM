/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 16:39
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 13:30
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.data.network.responses

import com.google.gson.annotations.SerializedName

data class RouteSectionPointResponse(
    @SerializedName("Direction")
    val direction: String,
    @SerializedName("ErrorMessage")
    val errorMessage: String?,
    @SerializedName("LinearId")
    val linearId: String,
    @SerializedName("PointLocation")
    val pointLocation: Double,
    @SerializedName("SectionId")
    val sectionId: Int,
    @SerializedName("BufferLocation")
    val bufferLocation: String

)
