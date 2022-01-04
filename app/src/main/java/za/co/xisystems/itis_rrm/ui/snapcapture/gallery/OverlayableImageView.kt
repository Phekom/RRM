package za.co.xisystems.itis_rrm.ui.snapcapture.gallery

import android.animation.LayoutTransition
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.data.localDB.entities.UnallocatedPhotoDTO
import za.co.xisystems.itis_rrm.databinding.ViewOverlayableImageBinding

class OverlayableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ViewOverlayableImageBinding.inflate(LayoutInflater.from(context), this)

    var image: UnallocatedPhotoDTO? = null
        set(value) {
            field = value
            value?.let {
                Glide.with(binding.imageView)
                    .load(it.photoPath)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(
                        FitCenter(),
                        RoundedCorners(resources.getDimensionPixelSize(R.dimen.rounded_corners_radius))
                    )
                    .into(binding.imageView)
            }
        }

    init {
        layoutTransition = LayoutTransition() // android:animateLayoutChanges="true"
        isActivated = false

        binding.sendButton.setOnClickListener {
            image?.let {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, it.photoPath)
                    type = "text/plain"
                }

                context.startActivity(sendIntent)
            }
        }
    }

    override fun setActivated(activated: Boolean) {
        val isChanging = activated != isActivated
        super.setActivated(activated)

        if (isChanging) {
            // Switch between VISIBLE and INVISIBLE
            binding.sendButton.isInvisible = !activated
        }
    }
}
