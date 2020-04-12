package za.co.xisystems.itis_rrm.logging

import android.util.Log

//
// Created by Shaun McDonald on 2020/03/27.
// Copyright (c) 2020 XI Systems. All rights reserved.
//

/** Not a real crash reporting library!  */
class LameCrashLibrary private constructor() {
    companion object {
        val TAG: String = LameCrashLibrary::javaClass.name

        fun log(priority: Int, tag: String?, message: String?) {
            // TODO add log entry to circular buffer.
            Log.println(priority, tag, message)
        }

        fun logWarning(t: Throwable?) {
            Log.w(TAG, t?.message)
            t?.printStackTrace()
        }

        fun logError(t: Throwable?) {
            Log.e(TAG, t?.message)
            t?.printStackTrace()
        }
    }

    init {
        throw AssertionError("No instances.")
    }
}