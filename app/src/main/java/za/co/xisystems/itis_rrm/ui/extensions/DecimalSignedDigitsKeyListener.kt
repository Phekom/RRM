/**
 * Created by Shaun McDonald on 2021/07/05
 * Last modified on 05/07/2021, 15:59
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.ui.extensions

import android.text.InputType
import android.text.method.DigitsKeyListener
import android.text.method.KeyListener
import java.text.DecimalFormatSymbols

class DecimalSignedDigitsKeyListener(digitsKeyListener: DigitsKeyListener) :
    KeyListener by digitsKeyListener {

    override fun getInputType() =
        InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL or
            InputType.TYPE_NUMBER_FLAG_SIGNED

    val separator = DecimalFormatSymbols.getInstance().decimalSeparator
}
