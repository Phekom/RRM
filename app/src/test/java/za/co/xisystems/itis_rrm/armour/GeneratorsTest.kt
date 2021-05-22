/**
 * Updated by Shaun McDonald on 2021/05/22
 * Last modified on 2021/05/22, 00:31
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Created by Shaun McDonald on 2021/05/22
 * Last modified on 2021/05/19, 21:32
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

/**
 * Updated by Shaun McDonald on 2021/05/19
 * Last modified on 2021/05/19, 08:38
 * Copyright (c) 2021.  XI Systems  - All rights reserved
 **/

package za.co.xisystems.itis_rrm.armour

import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratorsTest {

    @Test
    fun whenPasswordGeneratedUsingCommonsText_thenSuccessful() {
        val passGen = Generators()
        val password: String = passGen.generateCommonTextPassword()
        var lowerCaseCount = 0
        for (c in password.toCharArray()) {
            if (c.code >= 97 || c.code <= 122) {
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
            if (c.code >= 48 || c.code <= 57) {
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
            if (c.code >= 33 || c.code <= 47) {
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
            if (c.code >= 33 || c.code <= 47) {
                specialCharCount++
            }
        }
        println("Passay Password: $password")
        assertTrue("Password validation failed in Passay", specialCharCount >= 2)
    }
}
