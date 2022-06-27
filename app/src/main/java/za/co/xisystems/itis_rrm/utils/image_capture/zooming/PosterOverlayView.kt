package za.co.xisystems.itis_rrm.utils.image_capture.zooming

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import za.co.xisystems.itis_rrm.R

class PosterOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var onDeleteClick: (Poster) -> Unit = {}
    private var myView : View?= null

    init {
        myView =  View.inflate(context, R.layout.view_poster_overlay, this)
        setBackgroundColor(Color.TRANSPARENT)
    }

    @SuppressLint("SetTextI18n")
    fun update(poster: Poster, context: Context, ) {
        myView.apply {
          val posterLat = this?.findViewById<TextView>(R.id.posterOverlayLatitudeText)
            val posterLong = this?.findViewById<TextView>(R.id.posterOverlayLongitudeText)
            val posterCommet = this?.findViewById<TextView>(R.id.posterOverlayDescriptionText)
            posterLat?.text = "Latitude : ${poster.latitude}"
            posterLong?.text = "Longitude : ${poster.longitude}"
            posterCommet?.text = poster.description
            val posterShare = this?.findViewById<ImageView>(R.id.posterOverlayShareButton)
//            posterShare?.setOnClickListener { context.sendShareIntent(poster.url) }
            val posterDelete = this?.findViewById<ImageView>(R.id.posterOverlayDeleteButton)
            posterDelete?.setOnClickListener { onDeleteClick(poster) }
        }

    }


}

