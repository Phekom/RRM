/**
 * Updated by Shaun McDonald on 2021/06/14
 * Last modified on 14/06/2021, 03:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Created by Shaun McDonald on 2021/06/14
 * Last modified on 14/06/2021, 03:47
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.media.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Media(
    val id: Long,
    val uri: Uri,
    val path: String,
    val name: String,
    val size: String,
    val mimeType: String,
    val width: String?,
    val height: String?,
    val date: String,
    val favorite: Boolean = false,
    val trashed: Boolean = false
) : Parcelable
