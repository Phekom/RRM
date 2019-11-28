package za.co.xisystems.itis_rrm.data.network

import za.co.xisystems.itis_rrm.data.localDB.entities.UserRoleDTO

/**
 * Created by Francis Mahlava on 2019/11/25.
 */
interface RoleResponseListener {

    fun onStarted()
    fun onRoleSuccess(userRoles: List<UserRoleDTO>)
//    fun onSignOut(userDTO: UserDTO)

    fun onFailure(message: String)
}