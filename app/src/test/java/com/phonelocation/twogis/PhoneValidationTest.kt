package com.phonelocation.twogis

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhoneValidationTest {
    private val internationalPhone = Regex("^\\+[1-9][0-9]{7,14}$")

    @Test fun acceptsTajikistanInternationalNumber() {
        assertTrue(internationalPhone.matches("+992900000000"))
    }

    @Test fun rejectsNumberWithoutPlus() {
        assertFalse(internationalPhone.matches("992900000000"))
    }

    @Test fun rejectsTooShortNumber() {
        assertFalse(internationalPhone.matches("+99212"))
    }

    @Test fun rejectsLetters() {
        assertFalse(internationalPhone.matches("+992ABC000000"))
    }
}
