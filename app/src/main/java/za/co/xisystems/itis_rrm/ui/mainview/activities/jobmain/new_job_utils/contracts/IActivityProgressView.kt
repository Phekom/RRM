package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.contracts

import android.app.Activity
import za.co.xisystems.itis_rrm.data._commons.views.IProgressView

interface IActivityProgressView : IProgressView {
    val activity: Activity
}
