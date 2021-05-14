/**
 * Updated by Shaun McDonald on 2021/05/15
 * Last modified on 2021/05/14, 20:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/14
 * Last modified on 2021/05/14, 19:48
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/*
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 */

package za.co.xisystems.itis_rrm.armour

import junit.framework.Assert.assertTrue
import org.junit.Test

class GeneratorsTest {

    @Test
    fun whenPasswordGeneratedUsingCommonsText_thenSuccessful() {
        val passGen = Generators()
        val password: String = passGen.generateCommonTextPassword()
        var lowerCaseCount = 0
        for (c in password.toCharArray()) {
            if (c.toInt() >= 97 || c.toInt() <= 122) {
                lowerCaseCount++
            }
        }
        println("CommonsText Password: $password")
        assertTrue("Password validation failed in commons-text ", lowerCaseCount >= 2)
    }

    @Test
    fun whenPasswordGeneratedUsingCommonsLang3_thenSuccessful() {
        val passGen = Generators()
        val password: String = passGen.generateCommonsLang3Password()
        var numCount = 0
        for (c in password.toCharArray()) {
            if (c.toInt() >= 48 || c.toInt() <= 57) {
                numCount++
            }
        }
        println("CommonsLang3 Password: $password")
        assertTrue("Password validation failed in commons-lang3", numCount >= 2)
    }

    @Test
    fun whenPasswordGeneratedUsingSecureRandom_thenSuccessful() {
        val passGen = Generators()
        val password = passGen.generateSecureRandomPassword()
        var specialCharCount = 0
        for (c in password.toCharArray()) {
            if (c.toInt() >= 33 || c.toInt() <= 47) {
                specialCharCount++
            }
        }
        println("SecureRandom Password: $password")
        assertTrue("Password validation failed in Secure Random", specialCharCount >= 2)
    }

    @Test
    fun whenPasswordGeneratedUsingPassay_thenSuccessful() {
        val passGen = Generators()
        val password: String = passGen.generatePassayPassword()
        var specialCharCount = 0
        for (c in password.toCharArray()) {
            if (c.toInt() >= 33 || c.toInt() <= 47) {
                specialCharCount++
            }
        }
        println("Passay Password: $password")
        assertTrue("Password validation failed in Passay", specialCharCount >= 2)
    }
}
