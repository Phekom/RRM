/**
 * Created by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 11:42
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import androidx.security.crypto.MasterKey

/**
 * Sage provides cryptographic keys for master, preferences and files
 */
class Sage {
    fun generateMasterKey(context: Context): MasterKey {
        val masterKey = MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return masterKey
    }
}
