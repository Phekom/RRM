package za.co.xisystems.itis_rrm.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Created by Shaun McDonald on 2020/06/05.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 **/


/***
 * A one shot observer that snags the data we need, and then shuts down.
 *
 * @receiver LiveData<T>
 * @param lifecycleOwner LifecycleOwner
 * @param observer Observer<T>
 */
fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}