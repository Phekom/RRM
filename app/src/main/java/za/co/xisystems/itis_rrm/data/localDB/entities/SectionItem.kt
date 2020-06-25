package za.co.xisystems.itis_rrm.data.localDB.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SectionItem : Comparable<SectionItem> {
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: SectionItem): Int {
        return (description ?: "").compareTo(other.description ?: "")
    }
    @PrimaryKey
    var sectionItemId: String = null.toString()
    var itemCode: String? = null
    var description: String? = null

    constructor()

    constructor(id: String, code: String, desc: String) {
        this.sectionItemId = id
        this.itemCode = code
        this.description = desc
    }

    override fun hashCode(): Int {
        return description?.hashCode() ?: 0
    }

    override fun equals(other: Any?): Boolean {
        return if (other is SectionItem) {
            other.description.equals(description)
        } else
            super.equals(other)
    }

    override fun toString(): String {
        return description.toString()
    }
}
