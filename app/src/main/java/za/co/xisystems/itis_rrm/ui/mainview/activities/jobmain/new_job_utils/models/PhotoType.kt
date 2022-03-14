package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils.models

import java.io.Serializable

enum class PhotoType(private val value: String) : Serializable {
    START("start"),
    END("end");

    override fun toString(): String {
        return value
    }
}
