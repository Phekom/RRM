package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents

import android.content.Intent

abstract class AbstractIntent {
    companion object {
        const val PROJECT_ID = "projectId"
        const val JOB = "job"
        const val JOB_ID = "jobId"
        const val ITEM = "item"
        const val DELETE = "delete"

        const val REQUEST_CODE_SELECT_ITEM = 11
        const val REQUEST_CODE_EDIT_ESTIMATE = 21
        const val REQUEST_TAKE_PHOTO = 1
    }

    internal fun flags(intent: Intent): Intent {
        return intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
}
