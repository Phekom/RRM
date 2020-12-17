package za.co.xisystems.itis_rrm.domain

data class JobInfo(
    var jobId: String,
    var description: String,
    var startKm: Double,
    var endKm: Double,
    var routeSection: String
)
