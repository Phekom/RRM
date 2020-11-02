package za.co.xisystems.itis_rrm.data._commons.views

import android.app.Activity

interface IBaseActivity<T : Activity> {
    val activity: T
}
