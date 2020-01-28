package za.co.xisystems.itis_rrm.data._commons

import android.text.Editable
import android.text.TextWatcher

abstract class AbstractTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        onTextChanged(s.toString())
    }

    override fun afterTextChanged(s: Editable) {}
    abstract fun onTextChanged(text: String)
}
