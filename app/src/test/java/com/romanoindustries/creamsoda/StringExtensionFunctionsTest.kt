package com.romanoindustries.creamsoda

import com.romanoindustries.creamsoda.helpers.splitWithEmptyRemoved
import org.junit.Test
import org.junit.Assert.*

class StringExtensionFunctionsTest {

    @Test
    fun splitWithEmptyRemovedTest() {
        val text = "Hello  ,  hi.  , babe,cool"
        val split = text.splitWithEmptyRemoved(",")
        val expected = listOf("Hello", "hi.", "babe", "cool")
        for (i in split.indices) {
            assertEquals(expected[i], split[i])
        }
    }

}