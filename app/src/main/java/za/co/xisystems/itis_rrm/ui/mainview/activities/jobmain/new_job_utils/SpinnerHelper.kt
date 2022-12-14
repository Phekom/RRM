@file:Suppress("UNCHECKED_CAST")

package za.co.xisystems.itis_rrm.ui.mainview.activities.jobmain.new_job_utils

/**
 * Created by Francis Mahlava on 2019/12/29.
 */

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView


object SpinnerHelper {
    fun <T> setSpinner(
        context: Context,
        spinner: Spinner,
        values: List<T>?,
        labels: Array<String?>?,
        listener: SelectionListener<T>?
    ) {
        if (values != null) {

            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                labels!!
            )

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item = values[position]
                    listener?.onItemSelected(position, item!!)
//                    (parent.getChildAt(0) as TextView).textSize = 13f
                    (parent.getChildAt(0) as TextView).setTextColor(Color.GRAY)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    interface SelectionListener<T> {
        fun onItemSelected(position: Int, item: T)
    }
}
