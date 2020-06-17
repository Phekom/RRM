package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.adapters

//import android.R.layout
//import android.util.Log
//import android.view.View
//import android.widget.Filter
//import android.widget.Filterable
//import android.widget.TextView
//import za.co.xisystems.itis_rrm.data._commons.AbstractAdapter
//import za.co.xisystems.itis_rrm.data.localDB.entities.ProjectItemDTO
//import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IItemsAdapter
//import java.util.*
//
//internal class ItemsAdapter(
//    data: List<ProjectItemDTO?>?,
//    listener: OnItemClickListener<ProjectItemDTO?>?
//) : AbstractAdapter<ProjectItemDTO?>(data, layout.simple_list_item_1, listener),
//    Filterable, IItemsAdapter<ProjectItemDTO?> {
//
//    override fun createViewHolder(view: View?): AbstractViewHolder {
//        return object : AbstractViewHolder(view) {
//            var textView: TextView? = null
//
//            override fun init(v: View?) {
//                textView = v as TextView?
//            }
//
//            fun setData(position: Int, item: ProjectItemDTO) {
//                super.setData(position, item)
//                val text: String = item.itemCode.toString() + " " + item.descr
//                Log.d("x-i", item.projectId)
//                textView!!.text = text
//            }
//        }
//    }
//
//    override fun getFilter(): Filter {
//        return object : Filter() {
//            override fun performFiltering(constraint: CharSequence): FilterResults {
//                val filteredData: MutableList<ProjectItemDTO> = ArrayList<ProjectItemDTO>()
//                val originalData: MutableList<ProjectItemDTO?> = originalData
//                if (originalData != null) for (item in originalData) {
//                    val itemCode: String = item?.itemCode!!
//                    val descr: String = item.descr!!
//                    val query = constraint.toString().toLowerCase(Locale.ROOT)
//                    if (itemCode != null && itemCode.toLowerCase(Locale.ROOT)
//                            .contains(query) || descr != null && descr.toLowerCase(
//                            Locale.ROOT
//                        ).contains(
//                            query
//                        )
//                    ) {
//                        filteredData.add(item)
//                    }
//                }
//                val results = FilterResults()
//                results.values = filteredData
//                return results
//            }
//
//            override fun publishResults(
//                constraint: CharSequence,
//                results: FilterResults
//            ) {
//                val filteredData: List<ProjectItemDTO> =
//                    results.values as ArrayList<ProjectItemDTO>
//                updateData(filteredData)
//                notifyDataSetChanged()
//            }
//        }
//    }
//}
