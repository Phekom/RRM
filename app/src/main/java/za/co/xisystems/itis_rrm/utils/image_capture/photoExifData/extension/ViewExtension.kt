package za.co.xisystems.itis_rrm.utils.image_capture.photoExifData.extension

import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar


/**
 * Created by Francis.Mahlava on 2022/04/24.
 * Xi Systems
 * francis.mahlava@xisystems.co.za
 */

fun CoordinatorLayout.showSnackbar(@StringRes text: Int) {
    val snackbar = Snackbar.make(this, context.getText(text), Snackbar.LENGTH_LONG)
    snackbar.show()
}

fun CoordinatorLayout.showSnackbar(text: String) {
    val snackbar = Snackbar.make(this, text, Snackbar.LENGTH_LONG)
    snackbar.show()
}