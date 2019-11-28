package za.co.xisystems.itis_rrm.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat.getSystemService


/**
 * Created by Francis Mahlava on 2019/10/18.
 */
fun Context.toast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG ).show()
}

fun ProgressBar.show(){
    visibility = View.VISIBLE
}

fun ProgressBar.hide(){
    visibility = View.GONE
    setBackgroundColor(1)
}

fun View.snackbar(message: String){
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->

        snackbar.setAction("Ok") {
            val snackbarView = snackbar.view
            snackbar.dismiss()
        }
    }.show()
}


/**
 * Hide keyboard.
 */
fun Activity.hideKeyboard() {
    val view = this.getCurrentFocus()
    if (view != null) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
    }
}