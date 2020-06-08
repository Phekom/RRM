package za.co.xisystems.itis_rrm.ui.extensions

import android.app.Dialog
import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import pereira.agnaldo.previewimgcol.ImageCollectionView
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.GlideApp
import za.co.xisystems.itis_rrm.utils.zoomage.ZoomageView

/**
 * Created by Shaun McDonald on 2020/06/08.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/


fun ImageCollectionView.scaleForSize(context: Context, imageCount: Int) {
    when (imageCount) {
        1 -> {
            this.baseImageHeight = this.measuredHeight - 5
            this.maxImagePerRow = 1
            this.maxRows = 1
        }
        2, 3, 4 -> {
            this.baseImageHeight = (this.measuredHeight / 2) - 10
            this.maxImagePerRow = 2
            this.maxRows = 2
        }
        else -> {
            this.baseImageHeight = (this.measuredHeight / 4) - 20
            this.maxImagePerRow = 3
            this.maxRows = 4
        }
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


