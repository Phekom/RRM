package za.co.xisystems.itis_rrm.ui.mainview.create.edit_estimates.capture_utils.util

import android.net.Uri
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.GlideApp

fun ImageView.setDrawableImage(@DrawableRes resource: Int, applyCircle: Boolean = false) {
    val glide = Glide.with(this).load(resource)
    if (applyCircle) {
        glide.apply(RequestOptions.circleCropTransform()).into(this)
    } else {
        glide.into(this)
    }
}

fun ImageView.setLocalImage(imageUri: Uri, applyCircle: Boolean = false) {

    GlideApp.with(this)
        .load(imageUri)
        .centerCrop()
        .error(R.drawable.no_image)
        .into(this)
    

//    val glide = Glide.with(this).load(uri)
//    if (applyCircle) {
//        glide.apply(RequestOptions.circleCropTransform()).into(this)
//    } else {
//        glide.into(this)
//    }
}
