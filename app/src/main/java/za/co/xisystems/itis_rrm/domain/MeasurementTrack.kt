package za.co.xisystems.itis_rrm.domain

data class MeasurementTrack(
    var userId: String,
    var trackRouteId: String,
    var description: String,
    var direction: Int
)
