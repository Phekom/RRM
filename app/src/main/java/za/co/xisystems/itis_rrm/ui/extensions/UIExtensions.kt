
package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import pereira.agnaldo.previewimgcol.ImageCollectionView
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView

/**
 * Created by Shaun McDonald on 2020/06/08.
 * Last modified on 26/06/2021, 05:52
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

fun ImageCollectionView.scaleForSize(imageCount: Int) {
    when (imageCount) {
        in 0..1 -> {
            this.baseImageHeight = this.measuredHeight - 5
            this.maxImagePerRow = 1
            this.maxRows = 1
        }
        in 2..4 -> {
            this.baseImageHeight = (this.measuredHeight / 2) - 5
            this.maxImagePerRow = 2
            this.maxRows = 2
        }
        else -> {
            this.baseImageHeight = (this.measuredHeight / 4) - 5
            this.maxImagePerRow = 3
            this.maxRows = 4
        }
    }
}

fun ImageCollectionView.addZoomedImages(
    photoPaths: List<Pair<Uri, Bitmap?>>,
    activity: FragmentActivity
) {

    photoPaths.forEach { pair ->
        this.addImage(
            pair.second!!,
            object : ImageCollectionView.OnImageClickListener {
                override fun onClick(bitmap: Bitmap, imageView: ImageView) {
                    showZoomedImage(
                        pair.first,
                        activity
                    )
                }
            }
        )
    }
}

fun showZoomedImage(imageUrl: Uri, activity: FragmentActivity) {
    val dialog = Dialog(activity, R.style.dialog_full_screen)
    dialog.setContentView(R.layout.new_job_photo)
    val zoomageView =
        dialog.findViewById<ZoomageView>(R.id.zoomedImage)
    GlideApp.with(activity)
        .load(imageUrl)
        .into(zoomageView!!)
    dialog.show()
}
