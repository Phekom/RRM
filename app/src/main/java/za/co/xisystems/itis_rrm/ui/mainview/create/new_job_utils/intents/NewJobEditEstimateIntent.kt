package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents

import android.app.Activity
import android.content.Intent
import io.reactivex.annotations.NonNull
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO

class NewJobEditEstimateIntent : AbstractIntent(), INewJobEditEstimateIntent {
    override fun getJob(@NonNull intent: Intent?): JobDTO? {
        return intent?.extras?.getSerializable(JOB) as JobDTO?
    }

    override fun getItem(@NonNull intent: Intent?): ProjectItemDTO? {
        return intent?.extras?.getSerializable(ITEM) as ProjectItemDTO?
    }

    override fun startActivityForResult(@NonNull activity: Activity?, job: JobDTO?, item: ProjectItemDTO?) {
        val intent = flags(Intent(activity, null)
                .putExtra(JOB, job)
                .putExtra(ITEM, item))
        activity?.startActivityForResult(intent, REQUEST_CODE_EDIT_ESTIMATE)
    }
}