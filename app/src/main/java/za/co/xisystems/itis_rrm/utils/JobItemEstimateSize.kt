package za.co.xisystems.itis_rrm.utils

enum class JobItemEstimateSize(private val value: String) {
    POINT("point"),
    LINE("line"),
    POLYGON("polygon");

    fun getValue(): String {
        return value
    }
}
