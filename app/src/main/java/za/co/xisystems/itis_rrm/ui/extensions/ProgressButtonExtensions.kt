package za.co.xisystems.itis_rrm.ui.extensions

import android.graphics.Color
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.github.razir.progressbutton.DrawableButton
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import za.co.xisystems.itis_rrm.R

fun Button.initProgress(lifecycleOwner: LifecycleOwner) {
    lifecycleOwner.bindProgressButton(this)
    this.attachTextChangeAnimator {
        fadeInMills = 150
        fadeOutMills = 150
        textColor = Color.WHITE
    }
}

fun Button.startProgress(caption: String) {
    this.background = ContextCompat.getDrawable(this.context, R.drawable.round_corner_green)
    this.showProgress {
        buttonText = caption
        gravity = DrawableButton.GRAVITY_TEXT_END
        textMarginRes = R.dimen.progressMargin
        progressColor = Color.WHITE
        progressRadiusRes = R.dimen.smallRadius
        progressStrokeRes = R.dimen.stroke3
    }
}

fun Button.warnProgress(caption: String) {
    this.background = ContextCompat.getDrawable(this.context, R.drawable.round_corner_yellow)
    this.hideProgress(caption)
}

fun Button.doneProgress(caption: String) {
    this.background = ContextCompat.getDrawable(this.context, R.drawable.round_corner_green)
    this.stopProgress(caption)
}

fun Button.failProgress(caption: String) {
    this.background = ContextCompat.getDrawable(this.context, R.drawable.round_corner_orange)
    this.stopProgress(caption)
}

fun Button.stopProgress(caption: String) {
    this.hideProgress(caption)
}
