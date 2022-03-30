package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.utils.image_capture.model.ImagePickerConfig

/**
 * Created by Francis Mahlava on 2021/11/23.
 */
class JobCreationToolbar : RelativeLayout {

    private lateinit var titleText: TextView
    lateinit var backImage: AppCompatImageView
//    private lateinit var cameraImage: AppCompatImageView

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        View.inflate(context, R.layout.main_toolbar, this)
        if (isInEditMode) {
            return
        }
        titleText = findViewById(R.id.main_toolbar_title)
        backImage = findViewById(R.id.main_toolbar_back)

    }

//    fun config(config: ImagePickerConfig) {
//        setBackgroundColor(Color.parseColor(config.toolbarColor))
//
//        titleText.text = if (config.isFolderMode) config.folderTitle else config.imageTitle
//        titleText.setTextColor(Color.parseColor(config.toolbarTextColor))
//
//        doneText.text = config.doneTitle
//        doneText.setTextColor(Color.parseColor(config.toolbarTextColor))
//        doneText.visibility = if (config.isAlwaysShowDoneButton) View.VISIBLE else View.GONE
//
//        backImage.setColorFilter(Color.parseColor(config.toolbarIconColor))
//
//        cameraImage.setColorFilter(Color.parseColor(config.toolbarIconColor))
//        cameraImage.visibility = if (config.isShowCamera) View.VISIBLE else View.GONE
//    }

    fun setTitle(title: String?) {
        titleText.text = title
    }

//    fun showDoneButton(isShow: Boolean) {
//        doneText.visibility = if (isShow) View.VISIBLE else View.GONE
//    }

    fun setOnBackClickListener(clickListener: OnClickListener) {
        backImage.setOnClickListener(clickListener)
    }

//    fun setOnCameraClickListener(clickListener: OnClickListener) {
//        cameraImage.setOnClickListener(clickListener)
//    }
//
//    fun setOnDoneClickListener(clickListener: OnClickListener) {
//        doneText.setOnClickListener(clickListener)
//    }
}