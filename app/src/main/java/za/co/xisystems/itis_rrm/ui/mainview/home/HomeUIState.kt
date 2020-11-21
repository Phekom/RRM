package za.co.xisystems.itis_rrm.ui.mainview.home

data class HomeUIState (
    var userName: String = "",
    val networkEnabled: Boolean = false,
    var gpsEnabled: Boolean = false,
    val serviceHealthy: Boolean = false
)
