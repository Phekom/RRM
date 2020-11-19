package za.co.xisystems.itis_rrm.ui.auth

import za.co.xisystems.itis_rrm.data.localDB.entities.UserDTO

/**
 * Created by Francis Mahlava on 2019/10/23.
 */

interface AuthListener {
    fun onStarted()
    fun onSuccess(userDTO: UserDTO)
    fun onSignOut(userDTO: UserDTO)
    fun onFailure(message: String)
}
