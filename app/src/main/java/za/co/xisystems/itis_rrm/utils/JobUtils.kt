/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/15, 00:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.utils

import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.Locale
import za.co.xisystems.itis_rrm.data.localDB.entities.JobDTO
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimatesPhotoDTO

object JobUtils {
    fun formatCost(value: Double): String {
        val out = "R " + DecimalFormat("###,##0.00").format(value)
        return out.replace(",", " ")
    }

    private fun formatTotalCost(totalCost: Double): String {
        return String.format(Locale.US, "Total Cost: %s", formatCost(totalCost))
    }

    fun formatTotalCost(job: JobDTO?): String {
        Coroutines.main {
        }
        var quantity = 0.0
        var cost = 0.0
        job?.jobItemEstimates?.forEach { estimate ->
            quantity += estimate.qty
            cost += estimate.lineRate
        }
        return formatTotalCost(cost)
    }

    fun areQuantitiesValid(job: JobDTO?): Boolean {
        when {
            job?.jobItemEstimates.isNullOrEmpty() -> return false
            else -> {
                job?.jobItemEstimates?.forEach { estimate ->
                    if (estimate.qty < 0.01) return false
                }
                return true
            }
        }
    }

    fun sort(photos: ArrayList<JobItemEstimatesPhotoDTO>?): ArrayList<JobItemEstimatesPhotoDTO>? {
        // startPhoto = [0] & endPhoto = [1]
        if (photos != null) {
            Collections.sort(
                photos,
                Comparator { o1, o2 ->
                    if (o1 == null || o2 == null) return@Comparator 0 // this case should never happen
                    if (o1.isPhotoStart()) return@Comparator -1
                    if (!o2.isPhotoStart()) {
                        0
                    } else {
                        1
                    }
                }
            )
        }
        return photos
    }
}
