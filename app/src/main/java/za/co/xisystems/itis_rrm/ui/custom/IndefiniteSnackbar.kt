package za.co.xisystems.itis_rrm.ui.custom

import android.view.View
import com.google.android.material.snackbar.Snackbar
import za.co.xisystems.itis_rrm.R

//
// Created by Shaun McDonald on 2020/05/23.
// Copyright (c) 2020 XI Systems. All rights reserved.
//
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