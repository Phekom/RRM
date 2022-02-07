package za.co.xisystems.itis_rrm.utils.image_capture.helper

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import za.co.xisystems.itis_rrm.R

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
class GlideHelper {

    companion object {
        private val options: RequestOptions =
            RequestOptions().placeholder(R.drawable.logo_new_medium)
                .error(R.drawable.no_image)
                .centerCrop()

        fun loadImage(imageView: ImageView, uri: Uri) {
            Glide.with(imageView.context)
                .load(uri)
                .apply(options)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView)

        }
    }
}