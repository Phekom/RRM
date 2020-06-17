// package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.intents
//
// import android.content.Context
// import android.content.Intent
// import io.reactivex.annotations.NonNull
// import za.co.xisystems.itis_rrm.ui.mainview.activities.NewJobActivity
//
// class NewJobIntent(var context: Context) : AbstractIntent() {
//    fun getIntent(): Intent {
//        return flags(Intent(context, NewJobActivity::class.java))
//    }
//
//    fun getIntent(@NonNull jobId: String?): Intent {
//        return getIntent().putExtra(JOB_ID, jobId)
//    }
//
//    fun startActivity(jobId: String?) {
//        context.startActivity(getIntent(jobId))
//    }
//
//    fun startActivity() {
//        context.startActivity(getIntent())
//    }
//
//    fun getJobId(@NonNull intent: Intent): String? {
//        return intent.extras?.getString(JOB_ID, null)
//    }
// }
