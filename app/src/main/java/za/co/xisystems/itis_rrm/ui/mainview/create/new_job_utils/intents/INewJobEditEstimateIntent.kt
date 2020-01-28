package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents

import android.app.Activity
import android.content.Intent
import io.reactivex.annotations.NonNull
import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO

interface INewJobEditEstimateIntent {
    fun getJob(@NonNull intent: Intent?): JobDTO?

    fun getItem(@NonNull intent: Intent?): ProjectItemDTO?

    fun startActivityForResult(@NonNull activity: Activity?, job: JobDTO?, item: ProjectItemDTO?)
}