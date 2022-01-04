/**
 * Created by Shaun McDonald on 2021/06/08
 * Last modified on 08/06/2021, 17:22
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.media.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Image(
    val id: Long,
    val path: String,
    val name: String,
    val size: String,
    val width: String?,
    val height: String?,
    val date: String
) : Parcelable
