package za.co.xisystems.itis_rrm.ui.snapcapture.gallery

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import kotlin.math.roundToInt

class CarouselAdapter(private val images: List<UnallocatedPhotoDTO>) :
    RecyclerView.Adapter<CarouselAdapter.VH>() {

    private var hasInitParentDimensions = false
    private var maxImageWidth: Int = 0
    private var maxImageHeight: Int = 0
    private var maxImageAspectRatio: Float = 1f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // At this point [parent] has been measured and has valid width & height
        if (!hasInitParentDimensions) {
            maxImageWidth =
                parent.width - 2 * parent.resources.getDimensionPixelSize(R.dimen.gradient_width)
            maxImageHeight = parent.height
            maxImageAspectRatio = maxImageWidth.toFloat() / maxImageHeight.toFloat()
            hasInitParentDimensions = true
        }

        return VH(OverlayableImageView(parent.context))
    }

    override fun onBindViewHolder(vh: VH, position: Int) {
        val image = images[position]

        // Change aspect ratio
        val imageAspectRatio = image.aspectRatio
        val targetImageWidth: Int = if (imageAspectRatio < maxImageAspectRatio) {
            // Tall image: height = max
            (maxImageHeight * imageAspectRatio).roundToInt()
        } else {
            // Wide image: width = max
            maxImageWidth
        }
        vh.overlayableImageView.layoutParams = RecyclerView.LayoutParams(
            targetImageWidth,
            RecyclerView.LayoutParams.MATCH_PARENT
        )

        // Load image
        vh.overlayableImageView.image = image

        vh.overlayableImageView.setOnClickListener {
            val rv = vh.overlayableImageView.parent as RecyclerView
            rv.smoothScrollToCenteredPosition(position)
        }
    }

    override fun getItemCount(): Int = images.size

    class VH(val overlayableImageView: OverlayableImageView) :
        RecyclerView.ViewHolder(overlayableImageView)
}

