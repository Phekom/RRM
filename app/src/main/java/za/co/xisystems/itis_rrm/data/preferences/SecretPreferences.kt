/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.data.preferences

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecretPreferences(context: Context) {
    // Encrypted shared prefs for RRM
    companion object {
        const val ENCRYPTED_PREFS_FILENAME = "styles_and_colours"
    }
    val instance by lazy {
        EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_FILENAME,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    // Retrieve or Generate Master Key
    // Set up share prefs
    // DbPassPhrase = random array of 64bytes
    // FileSystemKey = for image encryption
}
