package za.co.xisystems.itis_rrm.data.provider

import za.co.xisystems.itis_rrm.internal.All_IDs

/**
 * Created by Francis Mahlava on 2019/12/12.
 */
interface ID_Provider {
    fun getIDs(): All_IDs
}
