package za.co.xisystems.itis_rrm.logging

import android.util.Log
import com.github.ajalt.timberkt.Timber

//
// Created by Shaun McDonald on 2020/03/27.
// Copyright (c) 2020 XI Systems. All rights reserved.
//

/** Not a real crash reporting library!  */
class LameCrashLibrary private constructor() {
    companion object {
        val TAG: String = LameCrashLibrary::javaClass.name

        fun log(priority: Int, tag: String?, message: String?) {
            Log.println(priority, tag, lameFix(message))
        }

        fun logWarning(t: Throwable?) {
            Timber.tag(TAG).w(lameFix(t?.message))
            t?.printStackTrace()
        }

        fun logError(t: Throwable?) {
            Timber.tag(TAG).e(lameFix(t?.message))
            t?.printStackTrace()
        }

        private fun lameFix(message: String?): String {
            return message ?: "No message provided"
        }
    }

    init {
        throw AssertionError("No instances.")
    }
}
