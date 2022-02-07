package za.co.xisystems.itis_rrm.utils.image_capture.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout


/**
 * Created by Francis Mahlava on 2021/11/23.
 */

class SquareFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}