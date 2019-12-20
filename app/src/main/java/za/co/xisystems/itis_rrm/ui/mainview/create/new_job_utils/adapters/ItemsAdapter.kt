package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.adapters

import android.R
import android.util.Log
import android.view.View
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import za.co.xisystems.itis_rrm.data._commons.AbstractAdapter
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IItemsAdapter
import java.util.*

internal class ItemsAdapter(
    data: List<ItemDTO?>?,
    listener: OnItemClickListener<ItemDTO?>?
) : AbstractAdapter<ItemDTO?>(data, R.layout.simple_list_item_1, listener),
    Filterable, IItemsAdapter<ItemDTO?> {

    override fun createViewHolder(view: View?): AbstractViewHolder {
        return object : AbstractViewHolder(view) {
            var textView: TextView? = null

            override fun init(v: View?) {
                textView = v as TextView?
            }

            fun setData(position: Int, item: ItemDTO) {
                super.setData(position, item)
                val text: String = item.itemCode.toString() + " " + item.descr
                Log.d("x-i", item.projectId)
                textView!!.text = text
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filteredData: MutableList<ItemDTO> = ArrayList<ItemDTO>()
                val originalData: MutableList<ItemDTO?> = getOriginalData()
                if (originalData != null) for (item in originalData) {
                    val itemCode: String = item?.itemCode!!
                    val descr: String = item.descr!!
                    val query = constraint.toString().toLowerCase()
                    if (itemCode != null && itemCode.toLowerCase().contains(query) || descr != null && descr.toLowerCase().contains(
                            query
                        )
                    ) {
                        filteredData.add(item)
                    }
                }
                val results = FilterResults()
                results.values = filteredData
                return results
            }

            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                val filteredData: List<ItemDTO> =
                    results.values as ArrayList<ItemDTO>
                updateData(filteredData)
                notifyDataSetChanged()
            }
        }
    }
}