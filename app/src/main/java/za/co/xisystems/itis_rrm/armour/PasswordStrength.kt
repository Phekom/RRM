/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/15, 00:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:57
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.armour

import android.graphics.Color
import za.co.xisystems.itis_rrm.R

enum class PasswordStrength(internal var resId: Int, color: Int) {

    WEAK(R.string.password_strength_weak, Color.RED),
    MEDIUM(R.string.password_strength_medium, Color.argb(255, 220, 185, 0)),
    STRONG(R.string.password_strength_strong, Color.GREEN),
    VERY_STRONG(R.string.password_strength_very_strong, Color.BLUE);

    var color: Int = 0
        internal set

    init {
        this.color = color
    }

    fun getText(ctx: android.content.Context): CharSequence {
        return ctx.getText(resId)
    }

    companion object {

        // This value defines the minimum length for the password
        internal var REQUIRED_LENGTH = 8

        // This value determines the maximum length of the password
        internal var MAXIMUM_LENGTH = 20

        // This value determines if the password should contain special characters. set it as "false" if you
        // do not require special characters for your password field.
        internal var REQUIRE_SPECIAL_CHARACTERS = true

        // This value determines if the password should contain digits. set it as "false" if you
        // do not require digits for your password field.
        internal var REQUIRE_DIGITS = true

        // This value determines if the password should require low case. Set it as "false" if you
        // do not require lower cases for your password field.
        internal var REQUIRE_LOWER_CASE = true

        // This value determines if the password should require upper case. Set it as "false" if you
        // do not require upper cases for your password field.
        internal var REQUIRE_UPPER_CASE = true

        fun calculateStrength(password: String): PasswordStrength {
            var currentScore = 0
            var sawUpper = false
            var sawLower = false
            var sawDigit = false
            var sawSpecial = false
            password.forEach { c ->
                when {
                    !sawSpecial && !Character.isLetterOrDigit(c) -> {
                        currentScore += 1
                        sawSpecial = true
                    }

                    !sawDigit && Character.isDigit(c) -> {
                        currentScore += 1
                        sawDigit = true
                    }

                    !sawUpper || !sawLower -> {
                        when {
                            Character.isUpperCase(c) -> sawUpper = true
                            else -> sawLower = true
                        }
                    }
                    sawUpper && sawLower -> currentScore += 1
                }
            }


            currentScore += scoreForLength(password, sawSpecial, sawUpper, sawLower, sawDigit, currentScore)

            when (currentScore) {
                0 -> return WEAK
                1 -> return MEDIUM
                2 -> return STRONG
                3 -> return VERY_STRONG
            }

            return VERY_STRONG
        }

        private fun scoreForLength(
            password: String,
            sawSpecial: Boolean,
            sawUpper: Boolean,
            sawLower: Boolean,
            sawDigit: Boolean,
            currentScore: Int
        ): Int {
            var currentScore1 = currentScore
            if (password.length > REQUIRED_LENGTH) {
                when {
                    REQUIRE_SPECIAL_CHARACTERS && !sawSpecial || REQUIRE_UPPER_CASE && !sawUpper || REQUIRE_LOWER_CASE && !sawLower || REQUIRE_DIGITS && !sawDigit -> {
                        currentScore1 = 1
                    }
                    else -> {
                        currentScore1 = 2
                        if (password.length > MAXIMUM_LENGTH) {
                            currentScore1 = 3
                        }
                    }
                }
            } else {
                currentScore1 = 0
            }
            return currentScore1
        }
    }
}
