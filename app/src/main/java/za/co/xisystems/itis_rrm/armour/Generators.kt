/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/15, 00:49
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:43
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */
package za.co.xisystems.itis_rrm.armour

import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.text.RandomStringGenerator.Builder
import org.passay.CharacterData
import org.passay.CharacterRule
import org.passay.EnglishCharacterData.Digit
import org.passay.EnglishCharacterData.LowerCase
import org.passay.EnglishCharacterData.UpperCase
import org.passay.PasswordGenerator
import java.security.SecureRandom
import java.util.Random
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

class Generators {
    private var random: Random = SecureRandom()
    fun generatePassayPassword(): String {
        val gen = PasswordGenerator()
        val lowerCaseChars: CharacterData = LowerCase
        val lowerCaseRule = CharacterRule(lowerCaseChars)
        lowerCaseRule.numberOfCharacters = 4
        val upperCaseChars: CharacterData = UpperCase
        val upperCaseRule = CharacterRule(upperCaseChars)
        upperCaseRule.numberOfCharacters = 4
        val digitChars: CharacterData = Digit
        val digitRule = CharacterRule(digitChars)
        digitRule.numberOfCharacters = 4
        val specialChars: CharacterData = object : CharacterData {
            override fun getErrorCode(): String {
                return ERROR_CODE
            }

            override fun getCharacters(): String {
                return ALLOWED_SPL_CHARACTERS
            }
        }
        val splCharRule = CharacterRule(specialChars)
        splCharRule.numberOfCharacters = 4
        return gen.generatePassword(16, splCharRule, lowerCaseRule, upperCaseRule, digitRule)
    }

    @RequiresApi(VERSION_CODES.N)
    fun generateCommonTextPassword(): String {
        val pwString = generateRandomSpecialCharacters(4) + generateRandomNumbers(4) + generateRandomAlphabet(4, true) + generateRandomAlphabet(4, false) + generateRandomNumbers(2)
        val pwChars = pwString.chars()
            .mapToObj { data: Int -> data.toChar() }
            .collect(Collectors.toList())
        pwChars.shuffle()
        return pwChars.stream()
            .collect({ StringBuilder() }, { stringBuilder: java.lang.StringBuilder?, obj: Char? -> stringBuilder?.append(obj) }) { obj: java.lang.StringBuilder, s: java.lang.StringBuilder? -> obj.append(s) }
            .toString()
    }

    @RequiresApi(VERSION_CODES.N)
    fun generateCommonsLang3Password(): String {
        val upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true)
        val lowerCaseLetters = RandomStringUtils.random(4, 97, 122, true, true)
        val numbers = RandomStringUtils.randomNumeric(4)
        val specialChar = RandomStringUtils.random(4, 33, 47, false, false)
        val totalChars = RandomStringUtils.randomAlphanumeric(2)
        val combinedChars = upperCaseLetters + lowerCaseLetters + numbers + specialChar + totalChars
        val pwdChars = combinedChars.chars()
            .mapToObj { c: Int -> c.toChar() }
            .collect(Collectors.toList())
        pwdChars.shuffle()
        return pwdChars.stream()
            .collect({ StringBuilder() }, { builder: java.lang.StringBuilder?, obj: Char? -> builder?.append(obj) }) { obj: java.lang.StringBuilder, s: java.lang.StringBuilder? -> obj.append(s) }
            .toString()
    }

    @RequiresApi(VERSION_CODES.N)
    fun generateSecureRandomPassword(): String {
        val pwdStream = Stream.concat(getRandomNumbers(4), Stream.concat(getRandomSpecialChars(4), Stream.concat(getRandomAlphabets(4, true), getRandomAlphabets(8, false))))
        val charList = pwdStream.collect(Collectors.toList())
        charList.shuffle()
        return charList.stream()
            .collect({ StringBuilder() }, { builder: java.lang.StringBuilder?, obj: Char? -> builder?.append(obj) }) { obj: java.lang.StringBuilder, s: java.lang.StringBuilder? -> obj.append(s) }
            .toString()
    }

    private fun generateRandomSpecialCharacters(length: Int): String {
        val pwdGenerator = Builder().withinRange(33, 45)
            .build()
        return pwdGenerator.generate(length)
    }

    private fun generateRandomNumbers(length: Int): String {
        val pwdGenerator = Builder().withinRange(48, 57)
            .build()
        return pwdGenerator.generate(length)
    }

    private fun generateRandomAlphabet(length: Int, lowerCase: Boolean): String {
        val low: Int
        val hi: Int
        if (lowerCase) {
            low = 97
            hi = 122
        } else {
            low = 65
            hi = 90
        }
        val pwdGenerator = Builder().withinRange(low, hi)
            .build()
        return pwdGenerator.generate(length)
    }

    @RequiresApi(VERSION_CODES.N)
    fun getRandomAlphabets(count: Int, upperCase: Boolean): Stream<Char?> {
        val characters: IntStream = if (upperCase) {
            random.ints(count.toLong(), 65, 90)
        } else {
            random.ints(count.toLong(), 97, 122)
        }
        return characters.mapToObj { data: Int -> data.toChar() }
    }

    @RequiresApi(VERSION_CODES.N)
    fun getRandomNumbers(count: Int): Stream<Char?> {
        val numbers = random.ints(count.toLong(), 48, 57)
        return numbers.mapToObj { data: Int -> data.toChar() }
    }

    @RequiresApi(VERSION_CODES.N)
    fun getRandomSpecialChars(count: Int): Stream<Char?> {
        val specialChars = random.ints(count.toLong(), 33, 45)
        return specialChars.mapToObj { data: Int -> data.toChar() }
    }

    companion object {
        /**
         * Special characters allowed in password.
         */
        const val ALLOWED_SPL_CHARACTERS = "!@#$%^&*()_+"
        const val ERROR_CODE = "ERRONEOUS_SPECIAL_CHARS"
    }
}
