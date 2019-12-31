@file:Suppress("UNCHECKED_CAST")

package za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

object SpinnerHelper {
    fun <T> setSpinner(
        context: Context,
        spinner: Spinner,
        data: List<T>?,
        datas : Array<String?>?,
        listener: SelectionListener<T>?
    ) {
        if (data != null) {

            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, datas!!)

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val item = data[position]
                    listener?.onItemSelected(position, item!!)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    interface SelectionListener<T> {
        fun onItemSelected(position: Int, item: T)
    }


}