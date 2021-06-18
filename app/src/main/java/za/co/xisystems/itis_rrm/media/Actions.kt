/**
 * Created by Shaun McDonald on 2021/06/14
 * Last modified on 14/06/2021, 20:12
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.media

import android.content.IntentSender

sealed class MainAction {
    data class ImagesChanged(val images: List<Media>) : MainAction()
    data class VideosChanged(val videos: List<Media>) : MainAction()
    data class FavoriteChanged(val favorites: List<Media>) : MainAction()
    data class TrashedChanged(val trashed: List<Media>) : MainAction()
    data class ScopedPermissionRequired(
        val intentSender: IntentSender,
        val forType: ModificationType
    ) : MainAction()

    object StoragePermissionsRequested : MainAction()
}

sealed class ImageDetailAction {
    object ImageDeleted : ImageDetailAction()
    object ImageSaved : ImageDetailAction()
    object ImageUpdated : ImageDetailAction()

    data class ScopedPermissionRequired(
        val intentSender: IntentSender,
        val forType: ModificationType
    ) : ImageDetailAction()
}

enum class ModificationType {
    UPDATE,
    DELETE,
    FAVORITE,
    TRASH
}
