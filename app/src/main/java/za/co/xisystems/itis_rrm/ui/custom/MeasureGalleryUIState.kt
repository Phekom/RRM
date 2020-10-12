package za.co.xisystems.itis_rrm.ui.custom

import android.graphics.Bitmap
import android.net.Uri
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO

/**
 * Created by Shaun McDonald on 2020/06/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/
data class MeasureGalleryUIState(
    val description: String?,
    val qty: Double,
    val lineRate: Double,
    val lineAmount: Double = 0.0,
    val photoPairs: List<Pair<Uri, Bitmap?>>,
    val jobItemMeasureDTO: JobItemMeasureDTO? = null
)
