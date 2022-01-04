package za.co.xisystems.itis_rrm.utils

enum class JobItemEstimateSize(private val value: String) {
    POINT("Point"),
    LINE("Line"),
    POLYGON("Polygon");

    fun getValue(): String {
        return value
    }
}
