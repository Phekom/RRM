/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 18:44
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.utils

import android.view.View
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.round

object Util {

    fun disableDoubleClick(view: View) {
        view.isEnabled = false
        view.postDelayed({ view.isEnabled = true }, 500)
    }

    fun insertDashUUID(uuid: String?): String {
        var sb = StringBuffer(uuid!!)
        sb.insert(8, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(13, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(18, "-")
        sb = StringBuffer(sb.toString())
        sb.insert(23, "-")
        return sb.toString()
    }

    fun nanCheck(toString: String): Boolean {
        return try {
            val dbl = toString.toDouble()
            dbl.isNaN()
        } catch (e: Exception) {
            true
        }
    }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    fun String.sha256(): String {
        val md = MessageDigest.getInstance("SHA-256")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}
