package za.co.xisystems.itis_rrm.utils

import android.text.TextUtils
import android.util.Patterns
import java.util.regex.Pattern
import android.text.Spanned

import android.text.InputFilter




/**
 * ValidateInputs class has different static methods, to validate different types of user Inputs
 */
object ValidateInputs {
	private const val blockCharacters = "[$&+~;=\\\\?@|/'<>^*()%!-]"

	//*********** Validate Email Address ********//
	fun isValidEmail(email: String?): Boolean {
		return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
	}

    //*********** Validate Email Address ********//
    fun isValidInputText(name: String): Boolean {
        val regExpn = "^([a-zA-Z ]{1,24})+$"
        if (name.equals("", ignoreCase = true)) return false
        val inputStr: CharSequence = name
        val pattern = Pattern.compile(blockCharacters, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(inputStr)
        return !pattern.matcher(inputStr).find()
    }


	//*********** Validate Name Input ********// 
	fun isValidName(name: String): Boolean {
		val regExpn = "^([a-zA-Z ]{1,24})+$"
		if (name.equals("", ignoreCase = true)) return false
		val inputStr: CharSequence = name
		val pattern = Pattern.compile(blockCharacters, Pattern.CASE_INSENSITIVE)
		val matcher = pattern.matcher(inputStr)
		return !pattern.matcher(inputStr).find()
	}

	//*********** Validate User Login ********//
	fun isValidLogin(login: String): Boolean {
		val regExpn = "^([a-zA-Z]{4,24})?([a-zA-Z][a-zA-Z0-9_]{4,24})$"
		val inputStr: CharSequence = login
		val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
		val matcher = pattern.matcher(inputStr)
		return matcher.matches()
	}

	//*********** Validate Password Input ********//
	fun isValidPassword(password: String): Boolean {
		val regExpn = "^[a-z0-9_$@.!%*?&]{6,24}$"
		val inputStr: CharSequence = password
		val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
		val matcher = pattern.matcher(inputStr)
		return matcher.matches()
	}

	//*********** Validate Phone Number********//
	fun isValidPhoneNo(phoneNo: String): Boolean {
		return phoneNo.length >= 10 && Patterns.PHONE.matcher(phoneNo).matches()
	}

	//*********** Validate Number Input ********//
	fun isValidNumber(number: String): Boolean {
		val regExpn = "^[0-9]{1,24}$"
		val inputStr: CharSequence = number
		val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
		val matcher = pattern.matcher(inputStr)
		return matcher.matches()
	}

	//*********** Validate Any Input ********//
	fun isValidInput(input: String): Boolean {
		val regExpn = "(.*?)?((?:[a-z][a-z]+))"
		if (input.equals("", ignoreCase = true)) return false
		val inputStr: CharSequence = input
		val pattern = Pattern.compile(blockCharacters, Pattern.CASE_INSENSITIVE)
		val matcher = pattern.matcher(inputStr)
		return !pattern.matcher(inputStr).find()
	}

	//*********** Validate Search Query ********//
	fun isValidSearchQuery(query: String): Boolean {
		val regExpn = "^([a-zA-Z]{1,24})?([a-zA-Z][a-zA-Z0-9_]{1,24})$"
		val inputStr: CharSequence = query
		val pattern = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE)
		val matcher = pattern.matcher(inputStr)
		return matcher.matches()
	}

    var EMOJI_FILTER = InputFilter { source, start, end, dest, dstart, dend ->
        for (index in start until end) {
            val type = Character.getType(source[index])
            if (type == Character.SURROGATE.toInt()) {
                return@InputFilter ""
            }
        }
        null
    }

}