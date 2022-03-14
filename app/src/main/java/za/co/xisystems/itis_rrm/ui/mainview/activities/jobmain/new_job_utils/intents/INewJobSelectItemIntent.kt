package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.intents

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import io.reactivex.annotations.NonNull
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO

interface INewJobSelectItemIntent {
    fun getIntent(@NonNull context: Context, projectId: String?): Intent

    fun startActivityForResult(@NonNull fragment: Fragment, projectId: String?)
    fun startActivityForResult(@NonNull activity: Activity, projectId: String?)

    fun getProjectId(@NonNull intent: Intent): JobDTO?
}
