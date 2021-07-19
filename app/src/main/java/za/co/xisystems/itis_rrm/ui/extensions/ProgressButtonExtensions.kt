package za.co.xisystems.itis_rrm.ui.extensions

import android.content.Context
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.github.razir.progressbutton.DrawableButton
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import za.co.xisystems.itis_rrm.R

/**
 * ProgressButton UI extensions for XI
 */

fun Button.initProgress(lifecycleOwner: LifecycleOwner, context: Context) {
    lifecycleOwner.bindProgressButton(this)
    this.attachTextChangeAnimator {
        fadeInMills = 100
        fadeOutMills = 100
        textColor = ContextCompat.getColor(context, R.color.progressTextColor)
    }
}

fun Button.startProgress(caption: String? = null, context: Context) {
    this.setBackgroundResource(R.drawable.round_corner_green)
    this.showProgress {
        buttonText = caption ?: "Loading"
        gravity = DrawableButton.GRAVITY_TEXT_END
        textMarginRes = R.dimen.progressMargin
        progressColor = ContextCompat.getColor(context, R.color.progressTextColor)
        progressRadiusRes = R.dimen.smallRadius
        progressStrokeRes = R.dimen.stroke3
    }
}

fun Button.doneProgress(caption: String? = null) {
    this.setBackgroundResource(R.drawable.round_corner_green)
    this.stopProgress(caption ?: "Complete")
}

fun Button.failProgress(caption: String? = null) {
    this.setBackgroundResource(R.drawable.round_corner_orange)
    this.stopProgress(caption ?: "Failed")
}

fun Button.stopProgress(caption: String? = null) {
    this.hideProgress(caption)
}
