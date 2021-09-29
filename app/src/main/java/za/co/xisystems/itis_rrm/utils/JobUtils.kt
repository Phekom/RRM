/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/15, 00:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.utils

import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
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
        val estimateCost = job?.jobItemEstimates?.filter { estimate ->
            estimate.size() == 2 && estimate.qty > 0.0 && estimate.lineRate > 0.0
        }?.map { it.qty * it.lineRate }?.fold(0.0, { total, item -> total + item }) ?: 0.0

        return formatTotalCost(estimateCost)
    }

    fun areQuantitiesValid(job: JobDTO?): Boolean {
        return when {
            job?.jobItemEstimates.isNullOrEmpty() -> false
            else -> {
                job?.jobItemEstimates?.forEach { estimate ->
                    if (estimate.qty < 0.01) return false
                }
                true
            }
        }
    }

    fun sort(photos: ArrayList<JobItemEstimatesPhotoDTO>?): ArrayList<JobItemEstimatesPhotoDTO>? {
        // startPhoto = [0] & endPhoto = [1]
        if (photos != null) {
            Collections.sort(photos, Comparator { o1, o2 ->
                if (o1 == null || o2 == null) return@Comparator 0 // this case should never happen
                if (o1.isStartPhoto()) return@Comparator -1
                if (!o2.isStartPhoto()) {
                    0
                } else {
                    1
                }
            })
        }
        return photos
    }

    fun isGeoCoded(job: JobDTO): Boolean {
        var result = true
        for (estimate in job.jobItemEstimates) {
            if (!estimate.geoCoded) {
                result = false
                break
            }
        }

        return result
    }
}
