package za.co.xisystems.itis_rrm.ui.custom

import android.graphics.Bitmap
import android.net.Uri
import za.co.xisystems.itis_rrm.data.localDB.entities.JobEstimateWorksDTO

data class WorksGalleryUIState(
    val photoPairs: List<Pair<Uri, Bitmap?>>,
    val comment: String,
    val worksDTO: JobEstimateWorksDTO
)
