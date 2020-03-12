package za.co.xisystems.itis_rrm.ui.mainview.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

//
// Created by Shaun McDonald on 2020/03/11.
// Copyright (c) 2020 XI Systems. All rights reserved.
//
@Suppress("UNCHECKED_CAST")
class SharedViewModelFactory : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SharedViewModel() as T
    }
}