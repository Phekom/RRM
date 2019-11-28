package za.co.xisystems.itis_rrm.data.models


import com.google.gson.annotations.SerializedName

data class Item(
    @SerializedName("Descr")
    val descr: String,
    @SerializedName("ItemCode")
    val itemCode: String,
    @SerializedName("ItemId")
    val itemId: String,
    @SerializedName("ItemSections")
    val itemSections: List<ItemSection>,
    @SerializedName("ProjectId")
    val projectId: String,
    @SerializedName("TenderRate")
    val tenderRate: Double,
    @SerializedName("Uom")
    val uom: String,
    @SerializedName("WorkflowId")
    val workflowId: Int
)