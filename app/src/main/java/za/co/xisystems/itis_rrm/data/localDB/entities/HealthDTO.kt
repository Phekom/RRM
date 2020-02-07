package za.co.xisystems.itis_rrm.data.localDB.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

@Entity
data class HealthDTO(
    @SerializedName("IsAlive")
    @PrimaryKey
    val isAlive: Int
)