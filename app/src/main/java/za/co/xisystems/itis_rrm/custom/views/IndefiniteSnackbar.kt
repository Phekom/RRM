package za.co.xisystems.itis_rrm.custom.views

import android.view.View
import com.google.android.material.snackbar.Snackbar
import za.co.xisystems.itis_rrm.R

/**
 * Created by Shaun McDonald on 2020/04/14.
 * Copyright (c) 2020 XI Systems. All rights reserved.
 */

/**
 * A floating snackbar which invites the user to retry a failed action.
 *
 */
object IndefiniteSnackbar {

    private var snackbar: Snackbar? = null

    fun show(view: View, text: String, action: () -> Unit) {
        snackbar = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE).apply {
            setAction(view.context.getString(R.string.retry)) { action() }
            show()
        }
    }

    fun hide() {
        snackbar?.dismiss()
    }
}
