/*
 * Copyright (c) 2020. Xtreme Intelligence Systems
 * Authors: Francis Mahlava & Shaun McDonald
 * All rights reserved.
 */

package za.co.xisystems.itis_rrm.ui.mainview.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import za.co.xisystems.itis_rrm.data.localDB.dao.JobDao
import za.co.xisystems.itis_rrm.data.localDB.entities.JobItemEstimateDTO
import za.co.xisystems.itis_rrm.data.repositories.JobCreationDataRepository

/**
 * Created by Shaun McDonald on 03 2020
 */
class CreateSavedStatedViewModel(
    private val handle: SavedStateHandle,
    private val jobCreationDataRepository: JobCreationDataRepository
) {

    val newJob: MutableLiveData<JobDao>? = null

    val editJob: LiveData<JobDao>? = null


}