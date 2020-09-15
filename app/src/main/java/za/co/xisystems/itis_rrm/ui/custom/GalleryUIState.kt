package za.co.xisystems.itis_rrm.ui.custom

import android.graphics.Bitmap
import android.net.Uri
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemMeasureDTO

/**
 * Created by Shaun McDonald on 2020/06/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/
data class GalleryUIState(
    var description: String?,
    var qty: Double,
    var lineRate: Double,
    var lineAmount: Double = 0.0,
    var photoPairs: List<Pair<Uri, Bitmap?>>,
    var measureItem: JobItemMeasureDTO
)
