package za.co.xisystems.itis_rrm.ui.extensions

import android.content.Context
import androidx.core.content.ContextCompat
import com.facebook.shimmer.Shimmer
import za.co.xisystems.itis_rrm.R

object ShimmerUtils {
    fun getGrayShimmer(context: Context): Shimmer {
        return Shimmer.ColorHighlightBuilder()
            .setBaseColor(ContextCompat.getColor(context, R.color.shimmerBase0))
            .setHighlightColor(ContextCompat.getColor(context, R.color.shimmerHighlight0))
            .setBaseAlpha(0.6f)
            .setHighlightAlpha(1.0f)
            .build()
    }
}

