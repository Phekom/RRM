package za.co.xisystems.itis_rrm.extensions

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.google.android.material.appbar.CollapsingToolbarLayout

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

/**
 * Extension from Google to allow notifying UI only for distinct operations in Room
 */
fun <T> LiveData<T>.getDistinct(): LiveData<T> {
    val distinctLiveData = MediatorLiveData<T>()
    distinctLiveData.addSource(this, object : Observer<T> {
        private var initialized = false
        private var lastObj: T? = null
        override fun onChanged(obj: T?) {
            if (!initialized) {
                initialized = true
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            } else if ((obj == null && lastObj != null) ||
                obj != lastObj
            ) {
                lastObj = obj
                distinctLiveData.postValue(lastObj)
            }
        }
    })
    return distinctLiveData
}

fun AppCompatActivity.applyToolbarMargin(toolbar: Toolbar) {
    toolbar.layoutParams = (
        toolbar.layoutParams
            as CollapsingToolbarLayout.LayoutParams
        ).apply {
            topMargin = getStatusBarSize()
        }
}

private fun AppCompatActivity.getStatusBarSize(): Int {
    val idStatusBarHeight =
        resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (idStatusBarHeight > 0) {
        resources.getDimensionPixelSize(idStatusBarHeight)
    } else {
        0
    }
}

fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}
