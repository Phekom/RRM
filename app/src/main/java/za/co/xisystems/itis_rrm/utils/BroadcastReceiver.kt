package za.co.xisystems.itis_rrm.utils

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.Gravity
import za.co.xisystems.itis_rrm.R
import za.co.xisystems.itis_rrm.ui.extensions.isOnline

class Connection : BroadcastReceiver() {
    lateinit var dialog: Dialog

    override fun onReceive(context: Context, intent: Intent) {

        dialog = Dialog(context)

//        dialog.apply {
//            setContentView(R.layout.dialog_connection)
//            setCancelable(false)
//            window!!.setBackgroundDrawableResource(android.R.color.transparent)
//            window!!.setGravity(Gravity.CENTER)
//            window!!.attributes.windowAnimations = R.style.AnimationPopup
//
//
//        }

        check(context = context) {
            if (!it) {
                dialog.show()
            } else {
                dialog.cancel()
            }
        }

    }

    private fun check(context: Context, result: (Boolean) -> Unit) {
        try {
            if (context.isOnline()) {
                result(true)
            } else {
                result(false)
            }

        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        }
    }
}