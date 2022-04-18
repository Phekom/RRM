package za.co.xisystems.itis_rrm.utils.image_capture.zooming


import android.content.Context
import androidx.appcompat.app.AlertDialog
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.image_capture.zooming.StylingOptions.Property.values
class StylingOptions {

    private val options = sortedMapOf(
        Property.HIDE_STATUS_BAR to true,
        Property.IMAGES_MARGIN to true,
        Property.CONTAINER_PADDING to false,
        Property.SHOW_TRANSITION to true,
        Property.SWIPE_TO_DISMISS to true,
        Property.ZOOMING to true,
        Property.SHOW_OVERLAY to true,
        Property.RANDOM_BACKGROUND to false)

    fun isPropertyEnabled(property: Property): Boolean {
        return options[property] == true
    }

    fun showDialog(context: Context) {
        AlertDialog.Builder(context)
            .setMultiChoiceItems(
                context.resources.getStringArray(R.array.styling_options),
                options.values.toBooleanArray()
            ) { _, indexSelected, isChecked ->
                options[values()[indexSelected]] = isChecked
            }.show()
    }


    enum class Property {
        HIDE_STATUS_BAR,
        IMAGES_MARGIN,
        CONTAINER_PADDING,
        SHOW_TRANSITION,
        SWIPE_TO_DISMISS,
        ZOOMING,
        SHOW_OVERLAY,
        RANDOM_BACKGROUND
    }
}