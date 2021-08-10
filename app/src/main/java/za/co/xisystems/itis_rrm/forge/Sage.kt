/**
 * Created by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 11:42
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.forge

import android.content.Context
import androidx.security.crypto.MasterKey
import androidx.security.crypto.MasterKey.Builder
import androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM
import kotlinx.coroutines.withContext

/**
 * Sage provides cryptographic keys for master, preferences and files
 */
class Sage(private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()) {

    suspend fun generateFutureMasterKey(context: Context): MasterKey {
        return withContext(dispatchers.default()) {
            return@withContext generateMasterKey(context)
        }
    }

    fun generateMasterKey(context: Context): MasterKey {
        return Builder(context.applicationContext)
            .setKeyScheme(AES256_GCM)
            .build()
    }
}
