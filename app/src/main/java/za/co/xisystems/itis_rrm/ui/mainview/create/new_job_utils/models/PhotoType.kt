package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models

import java.io.Serializable

enum class PhotoType(private val value: String) : Serializable {
    Start("start"),
    End("end");

    override fun toString(): String {
        return value
    }
}
