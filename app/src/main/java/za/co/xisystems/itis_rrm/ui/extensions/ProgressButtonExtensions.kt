package za.co.xisystems.itis_rrm.ui.extensions

import android.graphics.Color
import android.widget.Button
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


fun Button.initProgress(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.bindProgressButton(this)
    this.attachTextChangeAnimator {
        fadeInMills = 100
        fadeOutMills = 100
        textColor = Color.WHITE
    }
}

fun Button.startProgress(caption: String? = null) {
    this.setBackgroundResource(R.drawable.round_corner_green)
    this.showProgress {
        buttonText = caption ?: "Loading"
        gravity = DrawableButton.GRAVITY_TEXT_END
        textMarginRes = R.dimen.progressMargin
        progressColor = Color.WHITE
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
