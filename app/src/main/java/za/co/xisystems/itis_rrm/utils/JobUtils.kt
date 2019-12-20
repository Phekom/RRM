package za.co.xisystems.itis_rrm.utils

import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO
import java.text.DecimalFormat
import java.util.*


object JobUtils {
    fun formatCost(value: Double): String {
        val out = "R " + DecimalFormat("###,##0.00").format(value)
        return out.replace(",", " ")
    }

    fun formatTotalCost(totalCost: Double): String {
        return String.format(Locale.US, "Total Cost: %s", JobUtils.formatCost(totalCost))
    }

    fun formatTotalCost(job: JobDTO?): String {
        var quantity = 0.0
        var cost = 0.0
        if (job != null && job!!.jobItemEstimates != null)
            for (estimate in job!!.jobItemEstimates!!) {
                quantity += estimate.qty
                cost += estimate.lineRate
            }
        return formatTotalCost(cost)
    }

    fun areQuantitiesValid(job: JobDTO?): Boolean {
        if (job == null || job!!.jobItemEstimates == null || job!!.jobItemEstimates!!.isEmpty())
            return false
        else {
            for (estimate in job!!.jobItemEstimates!!) {
                // TODO clean precision if needed
                if (estimate.qty < 0.01) return false
            }
            return true
        }
    }

    // TODO this will not be needed
    fun compressJobEstimates(job: JobDTO?) {
        if (job != null && job!!.jobItemEstimates != null)
            for (jobItemEstimate in job!!.jobItemEstimates!!) {
                JobUtils.compressJobEstimates(jobItemEstimate)
            }
    }

    private fun compressJobEstimates(jobItemEstimate: JobItemEstimateDTO?) {
        if (jobItemEstimate == null) return

        var startPhoto = jobItemEstimate!!.getJobItemEstimatePhotoStart()
        var endPhoto = jobItemEstimate!!.getJobItemEstimatePhotoEnd()

        if (startPhoto == null || endPhoto == null) {
            val photos = jobItemEstimate!!.jobItemEstimatesPhotos
            if (photos != null && photos!!.size >= 2) {
                startPhoto = photos!!.get(0)
                endPhoto = photos!!.get(1)
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
        jobItemEstimate.jobItemEstimatesPhotos.clear()
        jobItemEstimate.jobItemEstimatesPhotos.add(startPhoto)
        jobItemEstimate.jobItemEstimatesPhotos.add(endPhoto)
    }

    fun sort(photos: ArrayList<JobItemEstimatesPhotoDTO>?): ArrayList<JobItemEstimatesPhotoDTO>? {
        // startPhoto = [0] & endPhoto = [1]
        if (photos != null) {
            Collections.sort(photos, Comparator<JobItemEstimatesPhotoDTO> { o1, o2 ->
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