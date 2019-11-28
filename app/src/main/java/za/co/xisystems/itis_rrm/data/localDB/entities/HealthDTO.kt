package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class HealthDTO(
    @PrimaryKey
    @SerializedName("IsAlive")
    val isAlive: Int
)