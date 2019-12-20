package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import io.reactivex.annotations.NonNull
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO


class NewJobSelectItemIntentFrag : AbstractIntent(), INewJobSelectItemIntent {
    override fun getIntent(@NonNull context: Context, projectId: String?): Intent {
        return flags(Intent(context.applicationContext, null)
                .putExtra(PROJECT_ID, projectId))

    }

    override fun startActivityForResult(fragment: Fragment, projectId: String?) {
        val intent = getIntent(fragment.context!!.applicationContext, projectId)
        fragment.startActivityForResult(intent, REQUEST_CODE_SELECT_ITEM)

    }

    override fun startActivityForResult(activity: Activity, projectId: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProjectId(@NonNull intent: Intent): JobDTO? {
        return intent.extras?.getSerializable(JOB) as JobDTO
    }
}