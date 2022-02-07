package za.co.xisystems.itis_rrm.utils.image_capture

import android.content.res.Configuration
import androidx.fragment.app.Fragment
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI

/**
 * Created by Francis Mahlava on 2021/11/23.
 */

abstract class BaseFragment : Fragment(), DIAware {
    override val di by closestDI()
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleOnConfigurationChanged()
    }

    abstract fun handleOnConfigurationChanged()
}