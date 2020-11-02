package za.co.xisystems.itis_rrm.utils.enums

/**
 * Created by Mauritz Mollentze on 2014/11/04.
 * Updated by Pieter Jacobs during 2016/08.
 */
enum class PhotoQuality(val value: Int) {
    ORIGINAL(1),
    HIGH(2), // ~4 MP
    MEDIUM(6), // ~1 MP
    LOW(8),
    THUMB(16),
    TINY(24);
}
