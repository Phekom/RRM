package za.co.xisystems.itis_rrm.utils

import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Locale

object JobUtils {
    fun formatCost(value: Double): String {
        val out = "R " + DecimalFormat("###,##0.00").format(value)
        return out.replace(",", " ")
    }

    private fun formatTotalCost(totalCost: Double): String {
        return String.format(Locale.US, "Total Cost: %s", formatCost(totalCost))
    }

    fun formatTotalCost(job: JobDTO?): String {
        var quantity = 0.0
        var cost = 0.0
        if (job?.JobItemEstimates != null)
            for (estimate in job.JobItemEstimates!!) {
                quantity += estimate.qty
                cost += estimate.lineRate
            }
        return formatTotalCost(cost)
    }

    fun areQuantitiesValid(job: JobDTO?): Boolean {
        if (job?.JobItemEstimates == null || job.JobItemEstimates!!.isEmpty())
            return false
        else {
            for (estimate in job.JobItemEstimates!!) {
//                 TODO clean precision if needed
                if (estimate.qty < 0.01) return false
            }
            return true
        }
    }

    // TODO this will not be needed
    fun compressJobEstimates(job: JobDTO?) {
        if (job?.JobItemEstimates != null)
            for (jobItemEstimate in job.JobItemEstimates!!) {
                compressJobEstimates(jobItemEstimate)
            }
    }

    private fun compressJobEstimates(jobItemEstimate: JobItemEstimateDTO?) {
        if (jobItemEstimate == null) return

        var startPhoto = jobItemEstimate.jobItemEstimatePhotos?.get(0)
        var endPhoto = jobItemEstimate.jobItemEstimatePhotos?.get(1)

        if (startPhoto == null || endPhoto == null) {
            val photos = jobItemEstimate.jobItemEstimatePhotos
            if (photos != null && photos.size >= 2) {
                startPhoto = photos[0]
                endPhoto = photos[1]
            }
        }
        setPhotos(jobItemEstimate, startPhoto, endPhoto)
    }

    private fun setPhotos(
        jobItemEstimate: JobItemEstimateDTO,
        startPhoto: JobItemEstimatesPhotoDTO?,
        endPhoto: JobItemEstimatesPhotoDTO?
    ) {
        if (startPhoto == null || endPhoto == null) return
        jobItemEstimate.jobItemEstimatePhotos?.clear()
        jobItemEstimate.jobItemEstimatePhotos?.add(startPhoto)
        jobItemEstimate.jobItemEstimatePhotos?.add(endPhoto)
    }

    fun sort(photos: ArrayList<JobItemEstimatesPhotoDTO>?): ArrayList<JobItemEstimatesPhotoDTO>? {
        // startPhoto = [0] & endPhoto = [1]
        if (photos != null) {
            Collections.sort(photos, Comparator { o1, o2 ->
                if (o1 == null || o2 == null) return@Comparator 0 // this case should never happen
                if (o1.isPhotoStart()) return@Comparator -1
                if (o2.isPhotoStart())
                    1
                else
                    0
            })
        }
        return photos
    }
}
