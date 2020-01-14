package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.models

import java.io.Serializable

enum class PhotoType private constructor(private val value: String) : Serializable {
    start("start"),
    end("end");

    override fun toString(): String {
        return value
    }
}
